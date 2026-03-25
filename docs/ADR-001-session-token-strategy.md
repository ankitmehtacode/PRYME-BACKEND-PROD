# ADR-001: Session Token Strategy
## Status: Accepted
## Date: 2026-03-26
## Context
The system requires a session token strategy that balances security, performance, and ease of implementation. The choice between opaque tokens and JWTs needs to be documented clearly.

Opaque tokens are server-side tokens stored in a secure database with cryptographic hashing, while JWTs are self-contained tokens that can be verified without querying the server. Given our current architecture and deployment constraints, we need to choose a strategy that aligns with our immediate needs and future scalability plans.

## Decision
We will use opaque tokens backed by PostgreSQL for session management. These tokens will be SHA-256 hashed at rest to ensure security. A Caffeine L1 read-through cache will be used to reduce database load, and we will enforce a 3-session-per-user limit to prevent abuse.

## Consequences
Positive:
- Enhanced security due to server-side token storage.
- Reduced risk of token leakage through client-side handling.
- Improved performance with caching.
- Simplicity in implementation without the complexity of JWT parsing and validation.

Negative:
- Requires database round-trip on cache miss, which can introduce latency.
- Additional complexity in managing session expiry and invalidation.
- Potential for increased database load if not properly cached.

## Future Migration Path
If JWTs are adopted later, we will need to modify the `SessionRecord` class to store JWT claims instead of opaque tokens. We will also add a dependency on a JWT library such as `jjwt`. The core application logic will remain largely unchanged, but the session management layer will require significant refactoring.
