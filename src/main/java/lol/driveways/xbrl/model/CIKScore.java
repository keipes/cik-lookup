package lol.driveways.xbrl.model;

public class CIKScore implements Comparable<CIKScore> {

    private Integer cik;
    private Float score;

    public CIKScore(final Integer cik, final Float score) {
        this.cik = cik;
        this.score = score;
    }

    public Integer getCik() {
        return cik;
    }

    public Float getScore() {
        return score;
    }

    public void addScore(final Integer score) {
        this.score = this.score + score;
    }

    @Override
    public int compareTo(final CIKScore o) {
        return o.score.compareTo(this.score);//this.score.compareTo(o.score);
    }

    public String toString() {
        return this.cik + " " + this.score;
    }
}
