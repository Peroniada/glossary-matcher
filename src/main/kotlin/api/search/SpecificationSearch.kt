package com.sperek.file.api.search

import com.sperek.file.api.Path

/**
 * Interface for searching files based on a set of specifications.
 */
interface SpecificationSearch {
    fun withRoot(path: Path): SpecificationSearch
    fun traversal(): SpecificationSearch
    fun withSpecification(specification: Specification<Path>): SpecificationSearch
    fun execute(): Sequence<Path>
}

/**
 * Represents a generic specification that checks if an object satisfies certain conditions.
 *
 * @param T the type of object being checked
 */
fun interface Specification<T> {
    fun isSatisfiedBy(t: T): Boolean
}

/**
 * Combines multiple specifications into a single specification that checks if all the specified specifications are satisfied.
 *
 * @param specs the array of specifications to be combined
 * @return a new specification that checks if all the specified specifications are satisfied
 */
fun <T> and(vararg specs: Specification<T>): Specification<T> {
    return Specification { t -> specs.all { it.isSatisfiedBy(t) } }
}

/**
 * Combines multiple specifications into a single specification using logical OR operation.
 *
 * @param specs The list of specifications to be combined.
 * @return A new specification that returns true if any of the given specifications is satisfied, false otherwise.
 *
 * @param T The type of the input that the specifications operate on.
 */
fun <T> or(vararg specs: Specification<T>): Specification<T> {
    return Specification { t -> specs.any { it.isSatisfiedBy(t) } }
}

/**
 * Returns a new Specification that negates the given specification.
 *
 * @param spec the specification to negate
 * @return a new Specification that represents the negation of the given specification
 * @param <T> the type of the object being evaluated by the specification
 * @see Specification
 */
fun <T> not(spec: Specification<T>): Specification<T> {
    return Specification { t -> !spec.isSatisfiedBy(t) }
}