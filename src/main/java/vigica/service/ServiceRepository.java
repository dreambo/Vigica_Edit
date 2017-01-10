package vigica.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vigica.model.DVBService;

@Repository
interface ServiceRepository extends JpaRepository<DVBService, Integer> {

	final static String SQL_1 = "select * FROM Service WHERE idx=:IDX AND type like %:TYPE% AND UPPER(name) like %:NAME% ORDER BY idx";
	final static String SQL_2 = "FROM DVBService WHERE UPPER(name) like UPPER(:NAME) ORDER BY idx";

	@Query(value=SQL_1, nativeQuery=true)
	public List<DVBService> getService(@Param("IDX") Integer idx, @Param("TYPE") String type, @Param("NAME") String name);

	@Query(SQL_2)
	public List<DVBService> getService(@Param("NAME") String name);
}
