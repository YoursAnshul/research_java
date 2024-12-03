package com.pro.api.models.dataaccess.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pro.api.models.dataaccess.ViPartLookupId;
import com.pro.api.models.dataaccess.ViPartLookupList;

@Repository
public interface ViPartLookupListRepository extends JpaRepository<ViPartLookupList, ViPartLookupId> {
    // Custom queries can be added here, if needed.
}
