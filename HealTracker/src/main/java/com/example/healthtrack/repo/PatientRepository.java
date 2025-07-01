package com.example.healthtrack.repo;


import com.example.healthtrack.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


// Note: This is my second attempt at this test task, and I hope this will be read.
// I want to mention that I overestimated my knowledge of databases and SQL in general. While working on this, I had to ask neural networks for help multiple times, and I had to redo queries several times because I don't know JPQL well.
// Even now, after making changes and corrections, I know that I probably haven't done enough and there is still room for optimization.
// I didn't add indexes, I could have explored ExecutorService in more depth, and normalized the database. Also, it's likely that VisitRepository could be further optimized.
// In any case, this process helped me expand my knowledge, and I am satisfied with the result. Thank you for the feedback!

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Rewrote using join, which is better for large amounts of data.
    // In the previous version, each record was checked individually, which took more time.
    // Explanation: a standard join, we join the visits table to patients to find patients with visits.
    // select distinct avoids duplicates when a patient has multiple visits.
    // The where clause finds patients by last name using like,
    // and checks if the doctorIDs parameter is null or if the doctor ID matches the provided values.
    // Returns the result as a page (suggested by GPT, I honestly didn't know this existed xD)
    @Query("""
    SELECT DISTINCT p FROM Patient p 
    LEFT JOIN p.visits v 
    WHERE (:search IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) 
          OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:doctorIds IS NULL OR v.doctor.id IN :doctorIds)
""")
    Page<Patient> searchPatients(
            @Param("search") String search,
            @Param("doctorIds") List<Long> doctorIds,
            Pageable pageable
    );

}