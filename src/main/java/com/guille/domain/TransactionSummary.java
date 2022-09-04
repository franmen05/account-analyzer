package com.guille.domain;

import java.util.Set;

public record TransactionSummary(Set<String> transactionsDesc, Float total) {}
