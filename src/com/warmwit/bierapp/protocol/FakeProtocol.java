package com.warmwit.bierapp.protocol;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.warmwit.bierapp.model.User;

public class FakeProtocol extends Protocol {

	@Override
	public void connect() {
		
	}

	@Override
	public void disconnect() {
		
	}

	public List<User> getUsers() {
		ArrayList<User> result = Lists.newArrayList();
		
		result.add(new User());
		result.add(new User());
		result.add(new User());
		result.add(new User());
		result.add(new User());
		result.add(new User());
		result.add(new User());
		
		return result;
	}
}
