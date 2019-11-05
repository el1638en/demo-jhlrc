package com.syscom.demojhlrc.dao;

import java.util.List;

import com.syscom.demojhlrc.beans.User;

public interface UserServiceESDAO {

	void createIndex();

	/**
	 * Créer un nouvel utilisateur.
	 * 
	 * @param user l'utilisateur à créer dans l
	 * @return
	 */
	String create(User user);

	/**
	 * 
	 * Rechercher un utilisateur à partir de son identifiant.
	 * 
	 * @param id identifiant de l'utilisateur
	 * @return
	 */
	User findById(String id);

	/**
	 * Modifier un utilisateur
	 * 
	 * @param user utilisateur à modifier.
	 * @return
	 */
	String update(User user);

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
