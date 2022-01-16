package com.sky.assignment.repository;

import com.sky.assignment.datamodel.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("clientRepository")
public interface ClientRepository extends CrudRepository<Client, String> {
}
