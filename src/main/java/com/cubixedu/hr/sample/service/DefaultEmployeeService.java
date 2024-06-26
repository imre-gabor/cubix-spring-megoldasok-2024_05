package com.cubixedu.hr.sample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cubixedu.hr.sample.config.HrConfigProperties;
import com.cubixedu.hr.sample.model.Employee;

@Service
@Qualifier("default")
public class DefaultEmployeeService extends AbstractEmployeeService {
	@Autowired
	HrConfigProperties config;

	@Override
	public int getPayRaisePercent(Employee employee) {
		return config.getSalary().getDef().getPercent();
	}

}
