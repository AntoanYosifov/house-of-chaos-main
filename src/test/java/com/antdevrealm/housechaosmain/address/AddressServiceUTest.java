package com.antdevrealm.housechaosmain.address;

import com.antdevrealm.housechaosmain.address.dto.AddressRequestDTO;
import com.antdevrealm.housechaosmain.address.model.AddressEntity;
import com.antdevrealm.housechaosmain.address.repository.AddressRepository;
import com.antdevrealm.housechaosmain.address.service.AddressService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceUTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    void givenValidAddressRequestDTO_whenCreate_thenAddressIsSavedAndReturned() {
        AddressRequestDTO requestDTO = new AddressRequestDTO("Bulgaria", "Sofia", 1000, "Main Street 1");

        AddressEntity savedEntity = AddressEntity.builder()
                .id(UUID.randomUUID())
                .country("Bulgaria")
                .city("Sofia")
                .zip(1000)
                .street("Main Street 1")
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(addressRepository.save(any(AddressEntity.class))).thenReturn(savedEntity);

        AddressEntity result = addressService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo("Bulgaria");
        assertThat(result.getCity()).isEqualTo("Sofia");
        assertThat(result.getZip()).isEqualTo(1000);
        assertThat(result.getStreet()).isEqualTo("Main Street 1");

        ArgumentCaptor<AddressEntity> captor = ArgumentCaptor.forClass(AddressEntity.class);
        verify(addressRepository, times(1)).save(captor.capture());

        AddressEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getCountry()).isEqualTo("Bulgaria");
        assertThat(capturedEntity.getCity()).isEqualTo("Sofia");
        assertThat(capturedEntity.getZip()).isEqualTo(1000);
        assertThat(capturedEntity.getStreet()).isEqualTo("Main Street 1");
        assertThat(capturedEntity.getCreatedOn()).isNotNull();
        assertThat(capturedEntity.getUpdatedAt()).isNotNull();
    }

    @Test
    void givenExistingAddressIdAndValidRequestDTO_whenUpdate_thenAddressIsUpdatedAndSaved() {
        UUID addressId = UUID.randomUUID();

        AddressRequestDTO requestDTO = new AddressRequestDTO("Germany", "Berlin", 10115, "Brandenburg Gate 1");

        AddressEntity existingEntity = AddressEntity.builder()
                .id(addressId)
                .country("Bulgaria")
                .city("Sofia")
                .zip(1000)
                .street("Main Street 1")
                .createdOn(Instant.now().minusSeconds(3600))
                .updatedAt(Instant.now().minusSeconds(3600))
                .build();

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(existingEntity));
        when(addressRepository.save(existingEntity)).thenReturn(existingEntity);

        AddressEntity result = addressService.update(requestDTO, addressId);

        assertThat(result).isEqualTo(existingEntity);
        assertThat(existingEntity.getCountry()).isEqualTo("Germany");
        assertThat(existingEntity.getCity()).isEqualTo("Berlin");
        assertThat(existingEntity.getZip()).isEqualTo(10115);
        assertThat(existingEntity.getStreet()).isEqualTo("Brandenburg Gate 1");
        assertThat(existingEntity.getUpdatedAt()).isNotNull();

        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, times(1)).save(existingEntity);
    }

    @Test
    void givenNonExistentAddressId_whenUpdate_thenResourceNotFoundExceptionIsThrown() {
        UUID addressId = UUID.randomUUID();

        AddressRequestDTO requestDTO = new AddressRequestDTO("Bulgaria", "Sofia", 1000, "Main Street 1");

        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.update(requestDTO, addressId));

        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, never()).save(any(AddressEntity.class));
    }
}
