package ib.edu.zpo_lista9;

import java.util.ArrayList;

public class AccelerationData {

    ArrayList<Double> xValues;
    ArrayList<Double> yValues;
    ArrayList<Double> zValues;

    public AccelerationData() {
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        zValues = new ArrayList<>();
    }

    public void addData(double x, double y, double z) {
        xValues.add(x);
        yValues.add(y);
        zValues.add(z);
    }

    public int countSteps() {
        int steps = 0;
        int left = 0;
        int right = 0;

        int neighbours = 20;

        for (int i = 0; i < yValues.size(); i++) {
            boolean isPeak = true;

            left = Math.max(0, i - neighbours);
            right = Math.min(yValues.size() - 1, i + neighbours);

            for (int j = left; j <= right; j++) {

                if (yValues.get(i) < yValues.get(j)) {
                    isPeak = false;
                    break;
                }
            }

            if (isPeak) {
                steps++;
                i += neighbours;
            }
        }
        return steps;
    }
}
