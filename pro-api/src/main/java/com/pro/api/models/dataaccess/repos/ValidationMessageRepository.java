package com.pro.api.models.dataaccess.repos;

import com.pro.api.models.business.ValidationMessagePlus;
import com.pro.api.models.dataaccess.ValidationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ValidationMessageRepository extends JpaRepository<ValidationMessage, Integer> {
	@Query(value = "SELECT * FROM ValidationMessages vm WHERE vm.DempoID = :dempoId " +
	       "AND EXTRACT(YEAR FROM vm.InMonth) = :year " +
	       "AND EXTRACT(MONTH FROM vm.InMonth) = :month", nativeQuery = true)
	List<ValidationMessage> findByDempoIdAndInMonthYearAndInMonthMonth(@Param("dempoId") String dempoId, @Param("year") int year, @Param("month") int month);

	@Query(value = "SELECT vm.ValidationMessagesID, vm.DempoID, vm.MessageID, vm.InMonth, vm.ScheduleKeys, vm.Details, vmt.MessageText " +
	       "FROM ValidationMessages vm " +
	       "JOIN ValidationMessageText vmt ON vm.MessageID = vmt.MessageId " +
	       "WHERE LOWER(vm.DempoID) = LOWER(:netId) " +
	       "AND EXTRACT(YEAR FROM vm.InMonth) = EXTRACT(YEAR FROM CAST(:inDate AS DATE)) " +
	       "AND EXTRACT(MONTH FROM vm.InMonth) = EXTRACT(MONTH FROM CAST(:inDate AS DATE))", nativeQuery = true)
	List<Object[]> findValidationMessagesByNetIdAndMonth(@Param("netId") String netId, @Param("inDate") LocalDate inDate);

	@Query(value = "SELECT vm.ValidationMessagesID, vm.DempoID, vm.MessageID, vm.InMonth, vm.ScheduleKeys, vm.Details, vmt.MessageText " +
	       "FROM ValidationMessages vm " +
	       "JOIN ValidationMessageText vmt ON vm.MessageID = vmt.MessageId " +
	       "WHERE EXTRACT(YEAR FROM vm.InMonth) = EXTRACT(YEAR FROM CAST(:inDate AS DATE)) " +
	       "AND EXTRACT(MONTH FROM vm.InMonth) = EXTRACT(MONTH FROM CAST(:inDate AS DATE))", nativeQuery = true)
	List<Object[]> findValidationMessagesByInDate(@Param("inDate") LocalDate inDate);

	@Query("SELECT vm FROM ValidationMessage vm WHERE vm.validationMessagesId IN :valMessageIds")
	List<ValidationMessage> findValidationMessagesByIds(@Param("valMessageIds") List<Integer> valMessageIds);

}
