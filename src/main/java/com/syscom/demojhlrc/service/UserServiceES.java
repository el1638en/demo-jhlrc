package com.syscom.demojhlrc.service;

import java.util.List;

import com.syscom.demojhlrc.beans.User;

/**
 * Contrat d'interface pour gérer les utilisateurs.
 *
 */
public interface UserServiceES {

	/**
	 * Créer l'index des utilisateurs.
	 * 
	 * @return
	 */
	void createIndex();

	/**
	 * Créer un nouvel utilisateur.
	 * 
	 * @param user l'utilisateur à créer dans l
	 * @return
	 */
	String create(User user);

	/**
	 * Modifier un utilisateur
	 * 
	 * @param user utilisateur à modifier.
	 * @return
	 */
	String update(User user);

	/**
	 * 
	 * Rechercher un utilisateur à partir de son identifiant.
	 * 
	 * @param id identifiant de l'utilisateur
	 * @return
	 */
	User findById(String id);

	/**
	 * 
	 * Supprimer un utilisateur à partir de son identificant.
	 * 
	 * @param id identifiant de l'utilisateur
	 * @return
	 */
	String deleteProfileDocument(String id);

	/**
	 * 
	 * Rechercher tous les utilisateurs.
	 * 
	 * @return liste des utilisateurs.
	 */
	List<User> findAll();

	/**
	 * 
	 * Rechercher les utilisateurs à partir d'un nom.
	 * 
	 * @param lastName nom de l'utilisateur
	 * @return liste des utilisateurs ce nom.
	 */
	List<User> searchByLastName(String lastName);

}
