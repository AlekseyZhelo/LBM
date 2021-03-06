package com.alekseyzhelo.lbm.cli;

import com.beust.jcommander.Parameter;

/**
 * @author Aleks on 21-02-2016.
 */
public class CLISettings {

    @Parameter(names = {"--time", "-t"}, description = "Simulation time steps", required = true)
    private Integer time = null;

    @Parameter(names = {"--length-x", "-lx"}, description = "Simulation rectangle X size (nodes).", required = false)
    private Integer lx = 256;

    @Parameter(names = {"--length-y", "-ly"}, description = "Simulation rectangle Y size (nodes).", required = false)
    private Integer ly = 256;

    @Parameter(names = {"--omega", "-o"}, description = "Reciprocal value of the relaxation parameter (1/tau).", required = true)
    private Double omega = null;

    @Parameter(names = {"--draw-velocities", "-vel"}, description = "Will draw velocities (instead of pressures).")
    private Boolean drawVelocities = false;

    @Parameter(names = {"--vector-field", "-vf"}, description = "Will draw the velocities as a vector field.")
    private Boolean vectorField = false;

    @Parameter(names = {"--stop", "-s"}, description = "Wait for any key input before starting the simulation.")
    private Boolean stop = false;

    @Parameter(names = {"--verbose", "-v"}, description = "Print step-by-step details.")
    private Boolean verbose = false;

    @Parameter(names = {"--no-collision", "-nc"}, description = "Do not calculate collisions.")
    private Boolean noCollisions = false;

    @Parameter(names = {"--no-rescale", "-nr"}, description = "Do not rescale the visualization parameter on every step.")
    private Boolean noRescale = false;

    @Parameter(names = {"--headless", "-h"}, description = "Do not do any visualization.")
    private Boolean headless = false;

    public Integer getTime() {
        return time;
    }

    public Double getOmega() {
        return omega;
    }

    public Integer getLx() {
        return lx;
    }

    public Integer getLy() {
        return ly;
    }

    // TODO: hack for image-specified lattice, improve

    public void setLx(Integer lx) {
        this.lx = lx;
    }

    public void setLy(Integer ly) {
        this.ly = ly;
    }

    public Boolean getDrawVelocities() {
        return drawVelocities;
    }

    public Boolean getVectorField() {
        return vectorField;
    }

    public Boolean getStop() {
        return stop;
    }

    public Boolean getVerbose() {
        return verbose;
    }

    public Boolean getNoCollisions() {
        return noCollisions;
    }

    public Boolean getNoRescale() {
        return noRescale;
    }

    public Boolean getHeadless() {
        return headless;
    }
}
