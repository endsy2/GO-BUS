package com.busapp.userservice.model;


import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> findByEmail(String email) {
        return (root,query,cb)->((
                    email==null||email.isEmpty()?cb.conjunction():cb.equal(root.get("email"),email)
                ));
    }
    public static Specification<User>findByUserName(String userName) {
        return (root,query,cb)
                ->((userName==null?cb.conjunction():cb.like(root.get("userName"),"%"+userName+"%")));
    }
    public static Specification<User>findByPhone (String phone){
        return (root,query,cb)
                ->phone==null|| phone.isEmpty() ?cb.conjunction():cb.equal(root.get("phone"),phone);
    }
    public static Specification<User>findByGoogleId(String googleId){
        return (root,query,cb)
                ->googleId==null||googleId.isEmpty()?cb.conjunction():cb.equal(root.get("googleId"),googleId);
    }
    public static Specification<User>findById(Long id){
        return (root,query,cb)
                ->id==null?cb.conjunction():cb.equal(root.get("id"),id);
    }
    public static Specification<User>findByIsEmployee(Boolean isEmployee){
        return (root,query,cb)->(
                isEmployee==null?cb.conjunction():cb.equal(root.get("isEmployee"),isEmployee)
                );
    }
    public static Specification<User> findByIsDeleted(Boolean isDelete){
        return ((root, query, cb) ->
                isDelete==null?cb.conjunction():cb.equal(root.get("isDeleted"),isDelete)
                );
    }
    public static Specification<User>findByIsActive(Boolean isActive){
        return ((root, query, cb) ->
                isActive==null?cb.conjunction():cb.equal(root.get("active"),isActive)
        );
    }

}
