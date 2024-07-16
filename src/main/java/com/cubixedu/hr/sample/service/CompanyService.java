package com.cubixedu.hr.sample.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cubixedu.hr.sample.model.Company;
import com.cubixedu.hr.sample.model.Employee;
import com.cubixedu.hr.sample.repository.CompanyRepository;

@Service
public class CompanyService {

	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private EmployeeService employeeService;
	
	@Transactional
	public Company save(Company company) {
		return companyRepository.save(company);
	}

	@Transactional
	public Company update(Company company) {
		if(companyRepository.existsById(company.getId()))
			return companyRepository.save(company);
		else
			return null;
	}

	public List<Company> findAll() {
		return companyRepository.findAll();
	}

	public Optional<Company> findById(long id) {
		return companyRepository.findById(id);
	}

	@Transactional
	public void delete(long id) {
		companyRepository.deleteById(id);
	}
	
	@Transactional
	public Company addEmployee(long id, Employee employee) {
		Company company = companyRepository.findByIdWithEmployees(id).get();
		Employee savedEmployee = employeeService.save(employee);
		company.addEmployee(savedEmployee);
		return company;
	}	
	
	@Transactional
	public Company deleteEmployee(long id, long employeeId) {
		Company company = companyRepository.findByIdWithEmployees(id).get();
		Employee employee = employeeService.findById(employeeId).get();
		employee.setCompany(null);
		company.getEmployees().remove(employee);
		//employeeService.save(employee); --> @Transactional miatt az employee menedzselt állapotú, sav nem szükséges
		return company;
	}
	
	@Transactional
	public Company replaceEmployees(long id, List<Employee> employees) {
		Company company = companyRepository.findByIdWithEmployees(id).get();
		company.getEmployees().forEach(e -> {
			e.setCompany(null);
		});
		company.getEmployees().clear();
		
		employees.forEach(e -> {
			Employee savedEmployee = employeeService.save(e);
			System.out.println(savedEmployee.getEmployeeId());
			company.addEmployee(savedEmployee);			
		});
		
		return company;
	}	
	
	
}
