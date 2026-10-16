package com.filmytell.support.repository;

import com.filmytell.support.entity.TicketSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketSubCategoryRepository extends JpaRepository<TicketSubCategory, Long> {
    List<TicketSubCategory> findByCategoryId(Long categoryId);
    Optional<TicketSubCategory> findByCategoryIdAndCode(Long categoryId, String code);
}
