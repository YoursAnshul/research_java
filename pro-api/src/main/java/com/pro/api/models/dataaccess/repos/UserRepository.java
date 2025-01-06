package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.dataaccess.User;
import com.pro.api.models.business.UserMin;
import com.pro.api.models.dataaccess.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Short> {

	List<User> findByActive(boolean active);

	// @Query("SELECT NEW com.pro.api.models.business.UserMin(u.userid, u.dempoid, u.fname, u.lname, u.language, u.trainedon, u.active, u.canedit, u.preferredfname, u.preferredlname, u.role, u.buddy, u.schedulinglevel) "
	// 		+ "FROM User u " + "ORDER BY "
	// 		+ "CASE WHEN u.preferredfname IS NOT NULL AND u.preferredfname != '' THEN u.preferredfname ELSE u.fname END ASC, "
	// 		+ "CASE WHEN u.preferredlname IS NOT NULL AND u.preferredlname != '' THEN u.preferredlname ELSE u.lname END ASC")
	@Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.fname, u.lname")
	List<UserMin> findAllUserMinsOrderedByName();

	@Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.fname, u.lname")
	List<User> findAllActiveUsersOrderedByName();

	@Query("SELECT u.userid FROM User u WHERE u.active = true ORDER BY u.userid")
	List<Short> findSortedActiveUserIds();
	
	 User findFirstByDempoid(String dempoid);
	 User findFirstByDempoidIgnoreCase(String dempoid);
	 User findFirstByEmailaddrIgnoreCase(String emailAddress);

	List<User> findByStatus(int i);
}
