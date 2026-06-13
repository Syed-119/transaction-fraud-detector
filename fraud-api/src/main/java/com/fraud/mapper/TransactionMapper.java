package com.fraud.mapper;

import com.fraud.dto.TransactionResponse;
import com.fraud.model.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toResponse(Transaction transaction);
}

