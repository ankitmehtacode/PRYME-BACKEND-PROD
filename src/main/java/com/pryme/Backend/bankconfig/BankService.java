

import com.pryme.Backend.common.ConflictException;
import com.pryme.Backend.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private final BankRepository bankRepository;

    // 🧠 SURGICAL CACHE MATRIX: Allows explicit, pinpoint eviction without annotations
    private final CacheManager cacheManager;

    // ==========================================
    // 🧠 HIGH-VELOCITY READ MATRIX
    // Automatically uses the Caffeine L1 Cache we defined in CacheConfig
    // ==========================================
    @Cacheable(cacheNames = "banks:all")
    @Transactional(readOnly = true)
    public List<BankResponse> getAll() {
        log.debug("L1 Cache Miss: Fetching Master Bank List from PostgreSQL");
        return bankRepository.findAll().stream().map(BankResponse::from).toList();
    }

    @Cacheable(cacheNames = "banks:partners")
    @Transactional(readOnly = true)
    public List<PartnerBankResponse> getActivePartners() {
        log.debug("L1 Cache Miss: Fetching Active Partners from PostgreSQL");
        return bankRepository.findTop15ByIsActiveTrueOrderByBankNameAsc()
                .stream()
                .map(PartnerBankResponse::from)
                .toList();
    }

    // ==========================================
    // 🧠 WRITE OPERATIONS & CACHE INVALIDATION
    // ==========================================
    @Transactional
    public BankResponse create(BankRequest request) {
        if (bankRepository.existsByBankNameIgnoreCase(request.bankName().trim())) {
            throw new ConflictException("Bank configuration with this exact name already exists.");
        }

        Bank bank = new Bank();
        bank.setBankName(request.bankName().trim());
        bank.setLogoUrl(request.logoUrl().trim());
        bank.setActive(request.isActive());

        Bank savedBank = bankRepository.save(bank);

        // Material change: Impacts recommendation engine (new active bank)
        surgicallyEvictCaches(true);

        log.info("Bank Config: Created new partner matrix for {}", savedBank.getBankName());
        return BankResponse.from(savedBank);
    }

    @Transactional
    public BankResponse update(UUID id, BankRequest request) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank footprint not found: " + id));

        String newName = request.bankName().trim();

        // 🧠 FAILPROOF DATA INTEGRITY: Prevent renaming to an already existing bank
        if (!bank.getBankName().equalsIgnoreCase(newName) && bankRepository.existsByBankNameIgnoreCase(newName)) {
            throw new ConflictException("Cannot rename. A bank with this name already dominates the matrix.");
        }

        boolean materialStatusChange = bank.isActive() != request.isActive();

        bank.setBankName(newName);
        bank.setLogoUrl(request.logoUrl().trim());
        bank.setActive(request.isActive());

        Bank savedBank = bankRepository.save(bank);

        // 🧠 SURGICAL EVICTION:
        // If they just fixed a logo typo, only clear the UI caches.
        // DO NOT nuke the heavy recommendation cache!
        surgicallyEvictCaches(materialStatusChange);

        log.info("Bank Config: Updated matrix for {}. Material Change: {}", savedBank.getBankName(), materialStatusChange);
        return BankResponse.from(savedBank);
    }

    @Transactional
    public BankResponse toggleVisibility(UUID id, boolean active) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank footprint not found: " + id));

        if (bank.isActive() == active) {
            return BankResponse.from(bank); // No-op, save DB overhead
        }

        bank.setActive(active);
        Bank savedBank = bankRepository.save(bank);

        // Toggling visibility is always a material change
        surgicallyEvictCaches(true);

        log.warn("Bank Config: Toggled visibility for {} to {}", savedBank.getBankName(), active);
        return BankResponse.from(savedBank);
    }

    @Transactional
    public void delete(UUID id) {
        if (!bankRepository.existsById(id)) {
            throw new NotFoundException("Bank footprint not found: " + id);
        }
        bankRepository.deleteById(id);

        surgicallyEvictCaches(true);
        log.warn("Bank Config: Executed hard delete on Bank ID {}", id);
    }

    // ==========================================
    // 🧠 THE SURGICAL EVICTION PROTOCOL
    // ==========================================
    private void surgicallyEvictCaches(boolean isMaterialChange) {
        // 1. Always clear UI/Static caches when any edit is made
        clearCacheSafely("banks:all");
        clearCacheSafely("banks:partners");

        // 2. ONLY clear the heavy computational cache if the bank was enabled/disabled
        if (isMaterialChange) {
            clearCacheSafely("banks:recommendation");
            log.warn("Surgical Cache Protocol: Heavy Recommendation Cache Obliterated due to Material Update.");
        } else {
            log.debug("Surgical Cache Protocol: Bypassed Recommendation Cache (Cosmetic Update Only).");
        }
    }

    private void clearCacheSafely(String cacheName) {
        try {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        } catch (Exception e) {
            log.error("Cache Engine Fault: Failed to safely evict matrix {}", cacheName, e);
        }
    }
}
