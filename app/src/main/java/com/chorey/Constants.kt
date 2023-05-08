package com.chorey

// Values
const val MAX_HOMES = 10
const val MAX_CHORES = 30
const val RANDOM_SEED = 42
const val MINS_MULTIPLIER = 7
// Funds multiplier assumes an average $20/hr wage - converts to minutes of work
const val EXPENSE_MULTIPLIER = 3 * MINS_MULTIPLIER
//TODO: Change this to BuildConfig.DEBUG when releasing
const val RUN_EMULATOR = false

const val DATE_PATTERN = "MMM dd, yyyy"
const val TIME_PATTERN = "hh:mm a"
const val DATE_TIME_PATTERN = "MMM dd, yyyy h:mm a"

// Database paths
const val HOME_COL = "homes"
const val USER_COL = "users"
const val CHORE_COL = "chores"
const val NOTE_COL = "notes"
const val INVITE_COL = "invites"
const val HISTORY_COL = "history"
const val OTHER_COL = "other"
const val EXPENSE_COL = "expenses"

const val DUMMY_FIELD = "UNAUTHENTICATED"