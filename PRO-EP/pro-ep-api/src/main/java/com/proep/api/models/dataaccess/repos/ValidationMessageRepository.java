package com.proep.api.models.dataaccess.repos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proep.api.models.business.ValidationMessagePlus;
import com.proep.api.models.dataaccess.ValidationMessage;

@Repository
public interface ValidationMessageRepository extends JpaRepository<ValidationMessage, Integer> {
	@Query("SELECT vm FROM ValidationMessage vm  WHERE vm.dempoId = :dempoId AND YEAR(vm.inMonth) = :year "
			+ "AND MONTH(vm.inMonth) = :month")
	List<ValidationMessage> findByDempoIdAndInMonthYearAndInMonthMonth(String dempoId, int year, int month);

	@Query("SELECT NEW com.proep.api.models.business.ValidationMessagePlus(vm, vmt.messageText) FROM ValidationMessage vm "
			+ "JOIN ValidationMessageText vmt ON vm.messageId = vmt.messageId "
			+ "WHERE LOWER(vm.dempoId) = LOWER(:netId) " + "AND YEAR(vm.inMonth) = YEAR(:inDate) "
			+ "AND MONTH(vm.inMonth) = MONTH(:inDate)")
	List<ValidationMessagePlus> findValidationMessagesByNetIdAndMonth(@Param("netId") String netId,
			@Param("inDate") LocalDate inDate);

	@Query("SELECT NEW com.proep.api.models.business.ValidationMessagePlus(vm, vmt.messageText) FROM ValidationMessage vm "
			+ "JOIN ValidationMessageText vmt ON vm.messageId = vmt.messageId "
			+ "WHERE YEAR(vm.inMonth) = YEAR(:inDate) " + "AND MONTH(vm.inMonth) = MONTH(:inDate)")
	List<ValidationMessagePlus> findValidationMessagesByInDate(@Param("inDate") LocalDate inDate);

	@Query("SELECT vm FROM ValidationMessage vm WHERE vm.validationMessagesId IN :valMessageIds")
	List<ValidationMessage> findValidationMessagesByIds(@Param("valMessageIds") List<Integer> valMessageIds);

}
