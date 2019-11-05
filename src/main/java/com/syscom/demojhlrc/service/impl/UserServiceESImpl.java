package com.syscom.demojhlrc.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.syscom.demojhlrc.beans.User;
import com.syscom.demojhlrc.dao.UserServiceESDAO;
import com.syscom.demojhlrc.service.UserServiceES;

/**
 * 
 * Implémentation du contrat d'interface des services dédiés aux utilisateurs.
 * 
 */
@Service
public class UserServiceESImpl implements UserServiceES {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceESImpl.class);

	private UserServiceESDAO userServiceESDAO;

	@Override
	public void createIndex() {
		userServiceESDAO.createIndex();
	}

	@Override
	public String create(User user) {
		logger.info("Create user {}.", user);
		Assert.notNull(user, "User must not null");
		return userServiceESDAO.create(user);
	}

	@Override
	public User findById(String id) {
		logger.info("Find user by id {}.", id);
		Assert.notNull(id, "Id User must not null");
		return userServiceESDAO.findById(id);
	}

	@Override
	public String update(User user) {
		logger.info("Update user {}.", user);
		Assert.notNull(user, "User must not null");
		return userServiceESDAO.update(user);
	}

	@Override
	public String deleteProfileDocument(String id) {
		logger.info("Delete user by id : {}.", id);
		Assert.notNull(id, "Id User must not null");
		return userServiceESDAO.deleteProfileDocument(id);
	}

	@Override
	public List<User> findAll() {
		logger.info("Find all users.");
		return userServiceESDAO.findAll();
	}

	@Override
	public List<User> searchByLastName(String lastName) {
		logger.info("Search users by last name {}.", lastName);
		Assert.notNull(lastName, "last name must not null");
		return userServiceESDAO.searchByLastName(lastName);
	}

}
