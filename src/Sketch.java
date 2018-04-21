import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Sketch extends PApplet {

    enum State {
        Free, Gather, Fish, Logo
    }

    private static Sketch sk = null;
    private float t;
    private State state = State.Free;
    private PImage logo;
    private float logoScale = 0.5f;
    private int logoBGColor = color(0, 0, 0, 255);
    private int defaultParticleColor = color(16, 51, 128, 255);

    final int swarmSize = 30;
    final int swarmNum = 100;
    private Swarm[] swarms;
    private List<Particle> particles;

    @Override
    public void settings() {
        if (Sketch.sk == null)
            Sketch.sk = this;
        size(1280, 720);
    }

    @Override
    public void setup() {
        loadLogo();

        createParticlesAndSwarms();

        frameRate(30);
        background(0);

        println("Done setup");
        println("----");
    }

    @Override
    public void draw() {
        noStroke();
        fill(0, 30);
        rect(0, 0, width, height);
        for (Swarm s: swarms) {
            s.update();
        }

        for (Swarm s: swarms) {
            s.render();
        }
        t += 0.01f;
    }

    private void loadLogo() {
        println("Loading Logos");
        logo = loadImage("data/logo.png");
    }

    private void createParticlesAndSwarms() {
        println("Create particles and swarms");
        // gravity center should be the center of the screen
        Particle.gravityCenter = new PVector(width / 2, height / 2);

        particles = new ArrayList<>();
        swarms = new Swarm[swarmNum];
        for (int i = 0; i < swarmNum; i += 1) {
            // particles is created in this constructor
            swarms[i] = new Swarm(i, swarmSize, true);
            particles.addAll(Arrays.asList(swarms[i].particles));
        }

        // init particles globally
        Particle.enableGravity = true;
        int particleGlobalId = 0;
        for (Particle p: particles) {
            float xRoot, yRoot;
            while (true) {
                xRoot = random(width);
                yRoot = random(height);
                int colorHit = logo.get((int) xRoot, (int) yRoot);
                if (alpha(colorHit) > 0) {
                    break;
                }
            }
            p.id = particleGlobalId;
            p.rootPos.set(xRoot, yRoot);
            p.loc.set(xRoot, yRoot);
            p.defaultColor = defaultParticleColor;
            p.noiseSeed = (float) p.id / 300;

            particleGlobalId += 1;
        }

        println("Done creating particles and swarms");
    }

    public float getTime() {
        return t;
    }

    public PImage getLogo() {
        return logo;
    }

    public State getState() {
        return state;
    }

    public static Sketch getSK() {
        return sk;
    }

    float perlinNoiseWithSeed(float seed) {
        return noise(seed, t);
    }

    float perlinNoiseWithSeedAndSwarmId(float seed, int swarmId) {
        return noise(swarmId * 10, seed, t);
    }
}
