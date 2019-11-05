package com.syscom.demojhlrc.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.syscom.demojhlrc.beans.User;
import com.syscom.demojhlrc.service.UserServiceES;

/**
 *
 * Controller pour gérer les utilisateurs.
 *
 */
@RestController
@RequestMapping(UserController.PATH)
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	public static final String PATH = "/api/users";

	@Autowired
	private UserServiceES userServiceES;

	@PostMapping
	public String createUser(@RequestBody User user) {
		logger.info("Create user {}.", user);
		return userServiceES.create(user);
	}

	@GetMapping("/{id}")
	public User findUserById(@PathVariable String id) {
		logger.info("Find user by id {}.", id);
		return userServiceES.findById(id);
	}

	@DeleteMapping("/{id}")
	public String deleteUser(@PathVariable String id) {
		logger.info("Delete user by id {}.", id);
		return userServiceES.deleteProfileDocument(id);

	}

	@GetMapping(value = "/search")
	public List<User> search(@RequestParam(value = "lastName") String lastName) {
		logger.info("Find user by last name {}.", lastName);
		return userServiceES.searchByLastName(lastName);
	}

	@GetMapping
	public List<User> findAll() {
		logger.info("Find all users.");
		return userServiceES.findAll();
	}

}
