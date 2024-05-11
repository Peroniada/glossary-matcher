package com.sperek.file.api.operation

import com.sperek.file.api.Path

/**
 * This interface represents a delete operation on a file or directory.
 */
interface DeleteOperation {
    fun file(path: Path): DeleteOperation
    fun perform(): DeleteResult
}

/**
 * This interface represents a rename operation, which is used to rename a file or directory.
 */
interface RenameOperation {
    fun file(path: Path): RenameOperation
    fun to(newName: String): RenameOperation
    fun perform(): RenameResult
}

/**
 * Represents an operation to move a file or directory.
 */
interface MoveOperation {
    fun file(path: Path): MoveOperation
    fun to(destination: Path): MoveOperation
    fun perform(): MoveResult
}

/**
 * Represents the result of a delete operation.
 *
 * @property success Indicates whether the delete operation was successful or not.
 */
data class DeleteResult(val success: Boolean)

/**
 * Represents the result of a move operation.
 *
 * @property newPath Specifies the new path of the moved item.
 * @property success Indicates whether the move operation was successful or not.
 */
data class MoveResult(val newPath: Path, val success: Boolean)

/**
 * Represents the result of renaming a file or directory.
 *
 * @property newPath The new path of the renamed file or directory.
 * @property success Indicates whether the renaming operation was successful.
 */
data class RenameResult(val newPath: Path, val success: Boolean)