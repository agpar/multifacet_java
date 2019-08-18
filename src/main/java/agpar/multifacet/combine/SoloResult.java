package agpar.multifacet.combine;

public class SoloResult {
    public String userId;
    public Double eliteYears;
    public Double eliteYearsNorm;
    public Double profileUp;
    public Double profileUpNorm;
    public Double fans;
    public Double fansNorm;
    public Double visibilty;
    public Double globalFeedback;
    public Double globalFeedbackNorm;
    public Double integrity;
    public Double competence;
    private static String template = "%s,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f";

    public SoloResult(String userId,
                      Double eliteYears,
                      Double eliteYearsNorm,
                      Double profileUp,
                      Double profileUpNorm,
                      Double fans,
                      Double fansNorm,
                      Double visibilty,
                      Double globalFeedback,
                      Double globalFeedbackNorm,
                      Double integrity,
                      Double competence) {
        this.userId = userId;
        this.eliteYears = eliteYears;
        this.eliteYearsNorm = eliteYearsNorm;
        this.profileUp = profileUp;
        this.profileUpNorm = profileUpNorm;
        this.fans = fans;
        this.fansNorm = fansNorm;
        this.visibilty = visibilty;
        this.globalFeedback = globalFeedback;
        this.globalFeedbackNorm = globalFeedbackNorm;
        this.integrity = integrity;
        this.competence = competence;
    }

    public static SoloResult fromString(String commaSeparated) {
        String[] splitString = commaSeparated.split(",");
        return new SoloResult(
                splitString[0],
                Double.parseDouble(splitString[1]),
                Double.parseDouble(splitString[2]),
                Double.parseDouble(splitString[3]),
                Double.parseDouble(splitString[4]),
                Double.parseDouble(splitString[5]),
                Double.parseDouble(splitString[6]),
                Double.parseDouble(splitString[7]),
                Double.parseDouble(splitString[8]),
                Double.parseDouble(splitString[9]),
                Double.parseDouble(splitString[10]),
                Double.parseDouble(splitString[11])
        );
    }

    public String toString() {
        return String.format(SoloResult.template,
                this.userId,
                this.eliteYears,
                this.eliteYearsNorm,
                this.profileUp,
                this.profileUpNorm,
                this.fans,
                this.fansNorm,
                this.visibilty,
                this.globalFeedback,
                this.globalFeedbackNorm,
                this.integrity,
                this.competence
            );
    }
}
