package com.cubixedu.hr.sample.security;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cubixedu.hr.sample.config.HrConfigProperties;
import com.cubixedu.hr.sample.config.HrConfigProperties.JwtData;
import com.cubixedu.hr.sample.model.Employee;

import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

	private static final String USERNAME = "username";
	private static final String MANAGED_EMPLOYEES = "managedEmployees";
	private static final String MANAGER = "manager";
	private static final String ID = "id";
	private static final String FULLNAME = "fullname";
	private static final String AUTH = "auth";
	private Algorithm algorithm;
	private String ISSUER;
	
	@Autowired
	private HrConfigProperties config;

	@PostConstruct
	public void init() {
		JwtData jwtData = config.getJwtData();
		ISSUER = jwtData.getIssuer();
		try {
			this.algorithm = (Algorithm) Algorithm.class.getMethod(jwtData.getAlg(), String.class).invoke(null, jwtData.getSecret());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public String createJwt(UserDetails userDetails) {
		
		Employee employee = ((HrUser)userDetails).getEmployee();
		Employee manager = employee.getManager();
		List<Employee> managedEmployees = employee.getManagedEmployees();
		
		Builder jwtBuilder = JWT.create()
			.withSubject(userDetails.getUsername())
			.withArrayClaim(AUTH, userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new))
			.withClaim(FULLNAME, employee.getName())
			.withClaim(ID, employee.getEmployeeId());
		
		
		if(manager != null) {
			jwtBuilder.withClaim(MANAGER, createMapFromEmployee(manager));
		}
		
		if(managedEmployees != null && !managedEmployees.isEmpty()) {
			jwtBuilder.withClaim(MANAGED_EMPLOYEES, managedEmployees.stream().map(this::createMapFromEmployee).toList());
		}

		return jwtBuilder
			.withExpiresAt(new Date(System.currentTimeMillis() + config.getJwtData().getDuration().toMillis()))
			.withIssuer(ISSUER)
			.sign(algorithm);
	}

	private Map<String, Object> createMapFromEmployee(Employee employee) {
		return Map.of(
				ID, employee.getEmployeeId(),
				USERNAME, employee.getUsername()
				);
	}
	
	private Employee parseEmployeeFromMap(Map<String, Object> employeeData) {
		if(employeeData == null)
			return null;
		Employee employee = new Employee();
		employee.setEmployeeId(((Integer)employeeData.get(ID)).longValue());
		employee.setUsername((String) employeeData.get(USERNAME));
		return employee;
	}

	public UserDetails parseJwt(String jwtToken) {
		
		DecodedJWT decodedJwt = JWT.require(algorithm)
		.withIssuer(ISSUER)
		.build()
		.verify(jwtToken);
		
		Employee employee = new Employee();
		employee.setEmployeeId(decodedJwt.getClaim(ID).asLong());
		employee.setUsername(decodedJwt.getSubject());
		employee.setName(decodedJwt.getClaim(FULLNAME).asString());
		
		Claim managerClaim = decodedJwt.getClaim(MANAGER);
		Map<String, Object> managerData = managerClaim.asMap();
		employee.setManager(parseEmployeeFromMap(managerData));
		
		
		Claim managedEmployeesClaim = decodedJwt.getClaim(MANAGED_EMPLOYEES);
		List<HashMap> managedEmployees = managedEmployeesClaim.asList(HashMap.class);
		if(managedEmployees != null) {
			employee.setManagedEmployees(managedEmployees.stream().map(this::parseEmployeeFromMap).toList());
		}
		
		return new HrUser(decodedJwt.getSubject(), "dummy", decodedJwt.getClaim(AUTH).asList(String.class).stream()
				.map(SimpleGrantedAuthority::new).toList(), employee);
	}

}
