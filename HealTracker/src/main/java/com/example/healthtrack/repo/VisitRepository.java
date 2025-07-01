package com.example.healthtrack.repo;

import com.example.healthtrack.dto.PatientVisitDTO;
import com.example.healthtrack.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    // This is the most complex query. It was initially written by GPT and then edited by me, but it's still not perfect.
    // The idea is to count the number of records in the visit table that satisfy the filtering conditions.
    // If there is at least one visit that overlaps with the new one, COUNT(v) will be greater than 0 and the query will return true. Otherwise, it returns false.
    // It checks all visits for the doctor with doctorId.
    // For each visit of the doctor, it checks if its time overlaps with the new visit using three conditions.
    @Query("""
        SELECT COUNT(v) > 0 FROM Visit v 
        WHERE v.doctor.id = :doctorId 
        AND (:start BETWEEN v.startDateTime AND v.endDateTime 
             OR :end BETWEEN v.startDateTime AND v.endDateTime
             OR v.startDateTime BETWEEN :start AND :end)
    """)
    boolean existsOverlappingVisit(
            @Param("doctorId") Long doctorId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );


    // This query selects the last visit of each patient to a specific doctor and returns extended information about the visit.
    // We use `SELECT new com.example.healthtrack.dto.PatientVisitDTO(...)` to create a DTO containing the required data.
    // `JOIN v.doctor d` allows us to get information about the doctor who saw the patient.
    // We filter only the patients passed in the `patientIds` parameter.
    // The subquery `MAX(v3.startDateTime)` finds the last visit of each patient to each doctor.
    // The subquery `(SELECT COUNT(DISTINCT v2.patient.id) FROM Visit v2 WHERE v2.doctor.id = d.id)` counts unique patients for the doctor.
    // This query is not the most optimal, but it works and is easy to understand. I couldn't come up with a better way, and GPT suggested that
    // an alternative could be to use `GROUP BY` or rewrite it with a more efficient JOIN instead of a subquery, as I did in the searchPatients method.
    @Query("""
       SELECT new com.example.healthtrack.dto.PatientVisitDTO(
          v.patient.id,
          v.doctor.id,
          v.startDateTime,
          v.endDateTime,
          d.firstName,
          d.lastName,
          (SELECT CAST(COUNT(DISTINCT v2.patient.id) AS long) FROM Visit v2 WHERE v2.doctor.id = d.id),
          d.timezone
       )
       FROM Visit v JOIN v.doctor d
       WHERE v.patient.id IN :patientIds
         AND v.startDateTime = (
             SELECT MAX(v3.startDateTime)
             FROM Visit v3
             WHERE v3.patient.id = v.patient.id AND v3.doctor.id = v.doctor.id
         )
    """)
    List<PatientVisitDTO> findLastVisitsByPatients(@Param("patientIds") List<Long> patientIds);

}