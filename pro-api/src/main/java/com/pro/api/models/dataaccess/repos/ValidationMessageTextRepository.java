package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.business.KeyValuePair;
import com.pro.api.models.dataaccess.ValidationMessageText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationMessageTextRepository extends JpaRepository<ValidationMessageText, Integer> {

	@Query("SELECT NEW com.pro.api.models.business.KeyValuePair(vmt.messageId, vmt.messageText) FROM ValidationMessageText vmt")
	List<KeyValuePair> findMessageIdAndMessageText();
}
