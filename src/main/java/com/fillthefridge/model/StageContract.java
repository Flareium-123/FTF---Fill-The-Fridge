package com.fillthefridge.model;

public enum StageContract {
    PRIORITIZE_PERISHABLE(
        "First to Safety",
        "Place the most urgent grocery before other food.",
        "Prioritize the shortest-life food first: it spends less time waiting on the counter."
    ),
    CLEAN_HANDLING(
        "Clean Handling",
        "Complete the sort without an invalid placement.",
        "Plan categories before placing items. Keeping raw foods sealed and separate helps prevent cross-contamination."
    ),
    COLD_CHAIN(
        "Protect the Cold Chain",
        "Keep every grocery from spoiling during this sort.",
        "Keep the fridge at or below 4°C / 40°F; refrigeration slows, but does not stop, bacterial growth."
    );

    private final String title;
    private final String objective;
    private final String educationalNote;

    StageContract(String title, String objective, String educationalNote) {
        this.title = title;
        this.objective = objective;
        this.educationalNote = educationalNote;
    }

    public String getTitle() {
        return title;
    }

    public String getObjective() {
        return objective;
    }

    public String getEducationalNote() {
        return educationalNote;
    }
}
