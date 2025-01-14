package com.proep.api.models.dataaccess.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proep.api.models.business.KeyValuePair;
import com.proep.api.models.dataaccess.ValidationMessageText;

@Repository
public interface ValidationMessageTextRepository extends JpaRepository<ValidationMessageText, Integer> {

	@Query("SELECT NEW com.proep.api.models.business.KeyValuePair(vmt.messageId, vmt.messageText) FROM ValidationMessageText vmt")
	List<KeyValuePair> findMessageIdAndMessageText();
}
