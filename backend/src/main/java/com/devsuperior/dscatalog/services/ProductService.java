package com.devsuperior.dscatalog.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable pageable) {
		Page<Product> page = productRepository.findAll(pageable);
		return page.map(x -> new ProductDTO(x, x.getCategories()));
	}

	@Transactional
	public ProductDTO findById(Long id) {
		Optional<Product> obj = productRepository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(entity, dto);
		entity = productRepository.save(entity);
		return new ProductDTO(entity, entity.getCategories());
	}
	
	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = productRepository.getReferenceById(id);
			copyDtoToEntity(entity, dto);
			entity = productRepository.save(entity);
			return new ProductDTO(entity, entity.getCategories());
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Product not found");
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public void delete(Long id) {
		if (!productRepository.existsById(id)) {
			throw new ResourceNotFoundException("ID not found: " + id);
		}
		try {
			productRepository.deleteById(id);
			
		}  catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	private void copyDtoToEntity(Product entity, ProductDTO dto) {
		entity.setName(dto.getName());
		entity.setPrice(dto.getPrice());
		entity.setDate(dto.getDate());
		entity.setDescription(dto.getDescription());
		entity.setImgUrl(dto.getImgUrl());
		
		entity.getCategories().clear();
		
		for(CategoryDTO catDto : dto.getCategories()) {
			if (!categoryRepository.existsById(catDto.getId())) {
				throw new ResourceNotFoundException("Category ID not found: " + catDto.getId());
			}
			Category cat = categoryRepository.getReferenceById(catDto.getId());
			entity.getCategories().add(cat);
		}
	}
}
