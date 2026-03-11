package com.pryme.Backend.cms;

import com.pryme.Backend.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "content:testimonials")
    public List<TestimonialResponse> publicTestimonials() {
        return testimonialRepository.findAllByActiveTrueOrderByFeaturedDescDisplayOrderAscCreatedAtDesc()
                .stream().map(TestimonialResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TestimonialResponse> all() {
        return testimonialRepository.findAll().stream().map(TestimonialResponse::from).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "content:testimonials", allEntries = true)
    public TestimonialResponse create(TestimonialRequest req) {
        Testimonial t = Testimonial.builder()
                .name(req.name().trim())
                .role(req.role().trim())
                .text(req.text().trim())
                .rating(req.rating() == null ? 5 : req.rating())
                .active(req.active() == null || req.active())
                .featured(req.featured() != null && req.featured())
                .displayOrder(req.displayOrder() == null ? 100 : req.displayOrder())
                .build();
        return TestimonialResponse.from(testimonialRepository.save(t));
    }

    @Transactional
    @CacheEvict(cacheNames = "content:testimonials", allEntries = true)
    public TestimonialResponse update(UUID id, TestimonialRequest req) {
        Testimonial t = testimonialRepository.findById(id).orElseThrow(() -> new NotFoundException("Testimonial not found"));
        t.setName(req.name().trim());
        t.setRole(req.role().trim());
        t.setText(req.text().trim());
        if (req.rating() != null) t.setRating(req.rating());
        if (req.active() != null) t.setActive(req.active());
        if (req.featured() != null) t.setFeatured(req.featured());
        if (req.displayOrder() != null) t.setDisplayOrder(req.displayOrder());
        return TestimonialResponse.from(testimonialRepository.save(t));
    }

    @Transactional
    @CacheEvict(cacheNames = "content:testimonials", allEntries = true)
    public void delete(UUID id) {
        if (!testimonialRepository.existsById(id)) throw new NotFoundException("Testimonial not found");
        testimonialRepository.deleteById(id);
    }
}
