package com.cubixedu.hr.sample.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cubixedu.hr.sample.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

	List<Employee> findBySalaryGreaterThan(Integer minSalary);

	List<Employee> findByPositionName(String title);

	List<Employee> findByNameStartingWithIgnoreCase(String name);

	List<Employee> findByDateOfStartWorkBetween(LocalDateTime start, LocalDateTime end);

	
	@Query("UPDATE Employee e "
			+ "SET e.salary = :minSalary "
			+ "WHERE e.company.id=:companyId "
			+ "AND e.position.name = :positionName "
			+ "AND e.salary < :minSalary")
	@Modifying	
	void updateSalaries(long companyId, String positionName, int minSalary);

	@EntityGraph(attributePaths = {"manager", "managedEmployees"})
	Optional<Employee> findByUsername(String username);
}
