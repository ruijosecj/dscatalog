package ruijosecj.dscatalog.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import ruijosecj.dscatalog.dto.CategoryDTO;
import ruijosecj.dscatalog.dto.ProductDTO;
import ruijosecj.dscatalog.entities.Category;
import ruijosecj.dscatalog.entities.Product;
import ruijosecj.dscatalog.repositories.CategoryRepository;
import ruijosecj.dscatalog.repositories.ProductRepository;
import ruijosecj.dscatalog.services.exceptions.DatabaseException;
import ruijosecj.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable  pageable) {
		Page<Product> list = repository.findAll(pageable);
		// Page<ProductDTO> listDTO = list.stream().map(x -> new
		// ProductDTO(x)).collect(Collectors.toList());
		return list.map(x -> new ProductDTO(x));
		// return listDTO;
	}

	//@Transactional(readOnly = true)
	//public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
	//	Page<Product> list = repository.findAll(pageRequest);
	//	// Page<ProductDTO> listDTO = list.stream().map(x -> new
	//	// ProductDTO(x)).collect(Collectors.toList());
	//	return list.map(x -> new ProductDTO(x));
	//	// return listDTO;
	//}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto,  entity);
		//entity.setName(dto.getName());
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getReferenceById(id);
			copyDtoToEntity(dto,  entity);
			//entity.setName(dto.getName());
			entity = repository.save(entity);
			return new ProductDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found" + id);
		}
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new ResourceNotFoundException("Recurso não encontrado");
		}
		try {
			repository.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Falha de integridade referencial");
		}
	}
	
	private void copyDtoToEntity(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();
		for(CategoryDTO catDTO : dto.getCategories()) {
			Category category =  categoryRepository.getReferenceById(catDTO.getId());
			entity.getCategories().add(category);
		}
	}
}
