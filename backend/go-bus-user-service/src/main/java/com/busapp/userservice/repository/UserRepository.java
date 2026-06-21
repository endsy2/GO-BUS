package com.busapp.userservice.repository;

import com.busapp.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByUserNameAndIdNot(String userName, Long id);
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByPhone(String phone);

    /**
     * Optimized query to fetch user with only role IDs (not full role objects).
     * This avoids loading permissions eagerly, which we'll fetch from cache instead.
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    /**
     * Fetch user + roles + permissions in one query by ID (for token issuance / detail views).
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);

    /**
     * Same as above but keyed by email — used by login so we avoid a lazy-load
     * of roles/permissions outside a transaction after the basic findByEmail.
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    /**
     * Admin user-detail view: fetches user + roles + permissions + wallet in a single round-trip.
     * Replaces 4 separate SELECTs (user, roles, per-role permissions N+1, wallet) for the
     * admin getUserById endpoint.
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "LEFT JOIN FETCH u.wallet " +
           "WHERE u.id = :id")
    Optional<User> findDetailById(@Param("id") Long id);

    /**
     * Fetch only basic user fields using native SQL query.
     * This avoids loading the entire User entity and its relationships.
     * Only selects: id, userName, fullName, email, phone
     */
    @Query(value = "SELECT u.id, u.user_name, u.full_name, u.email, u.phone FROM \"user_service\".\"user\" u WHERE u.id =:id",
            nativeQuery = true)
    Optional<Object[]> findBasicByIdNative(@Param("id") Long id);

    /**
     * Fetch basic user fields for multiple users using native SQL.
     */
    @Query(value = "SELECT u.id, u.user_name, u.full_name, u.email, u.phone FROM \"user_service\".\"user\" u WHERE u.id IN (:ids)",
            nativeQuery = true)
    List<Object[]> findBasicByIdsNative(@Param("ids") Set<Long> ids);

}