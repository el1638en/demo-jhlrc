package com.syscom.demojhlrc.beans;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

	private String id;
	private String firstName;
	private String lastName;
	private List<String> emails;

}