package com.cubixedu.hr.sample.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cubixedu.hr.sample.model.Employee;
import com.cubixedu.hr.sample.model.Position;
import com.cubixedu.hr.sample.repository.EmployeeRepository;
import com.cubixedu.hr.sample.repository.PositionRepository;

public abstract class AbstractEmployeeService implements EmployeeService {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private PositionRepository positionRepository;
	
	@Override
	public Employee save(Employee employee) {
		adjustPosition(employee);
		
		return employeeRepository.save(employee);
	}

	private void adjustPosition(Employee employee) {
		Position position = null;
		String positionName = employee.getPosition().getName();
		if(positionName != null) {
			List<Position> positions = positionRepository.findByName(positionName);
			if(positions.isEmpty()) {
				position = positionRepository.save(employee.getPosition());
			} else {
				position = positions.get(0);
			}
		}
		employee.setPosition(position);
	}

	@Override
	public Employee update(Employee employee) {
		if(employeeRepository.existsById(employee.getEmployeeId())) {
			adjustPosition(employee);
			return employeeRepository.save(employee);
		} else
			return null;
	}

	@Override
	public List<Employee> findAll() {
		return employeeRepository.findAll();
	}

	@Override
	public Optional<Employee> findById(long id) {
		return employeeRepository.findById(id);
	}

	@Override
	public void delete(long id) {
		employeeRepository.deleteById(id);
	}
	
}
