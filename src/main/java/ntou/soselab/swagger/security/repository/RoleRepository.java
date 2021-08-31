package ntou.soselab.swagger.security.repository;

import ntou.soselab.swagger.security.model.ERole;
import ntou.soselab.swagger.security.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
