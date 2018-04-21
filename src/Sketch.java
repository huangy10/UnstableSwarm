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
    private float logoScale = 1f;
    private int defaultParticleColor = color(80);
    final int swarmSize = 40;
    final int swarmNum = 100;
    private Swarm[] swarms;
    private List<Swarm> swarmHeaders;
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
        GatherMovePattern.defaultPattern.gravityCenter = new PVector(width / 2, height / 2);
        LogoMovePattern.defaultPattern.gravityCenter = new PVector(width / 2, height / 2);

        createParticlesAndSwarms();

        frameRate(30);
        background(255);

        ParticleMovePattern.setGlobalEnabledPattern(GatherMovePattern.defaultPattern);

        println("Done setup");
        println("----");
    }

    @Override
    public void draw() {
//        noStroke();
//        fill(255, 50);
//        rect(0, 0, width, height);
        background(255);
        for (Swarm s: swarms) {
            s.update();
        }

        for (Swarm s: swarms) {
            s.render();
        }
        t += 0.01f;
        surface.setTitle("Framerate: " + frameRate);
    }

    @Override
    public void mousePressed() {
        if (state == State.Gather) {
            ParticleMovePattern.setGlobalEnabledPattern(LogoMovePattern.defaultPattern);
            state = State.Logo;
        } else {
            ParticleMovePattern.setGlobalEnabledPattern(GatherMovePattern.defaultPattern);
            state = State.Gather;
        }
    }

    private void loadLogo() {
        println("Loading Logos");
        logo = loadImage("data/logo.png");
    }

    private void createParticlesAndSwarms() {
        println("Create particles and swarms");

        particles = new ArrayList<>();
        swarmHeaders = new ArrayList<>();
        swarms = new Swarm[swarmNum];
        for (int i = 0; i < swarmNum; i += 1) {
            // particles is created in this constructor
            swarms[i] = new Swarm(i, swarmSize, true);
            swarms[i].clusterLeaders = swarmHeaders;
            swarms[i].setEnabled(true);
            particles.addAll(Arrays.asList(swarms[i].particles));
        }
        for (int i = 0; i < 3; i+= 1) {
            swarms[i].setAsAttractionHeader();
        }

        // init particles globally
        Particle.enableGravity = true;
        int particleGlobalId = 0;
        float logoWidth = logo.width * logoScale;
        float logoHeight = logo.height * logoScale;
        float logoOffsetX = (width - logoWidth) / 2;
        float logoOffsetY = (height - logoHeight) / 2;
        logo.loadPixels();
        for (Particle p: particles) {
            float xRoot, yRoot;
            int colorHit;
            while (true) {
                xRoot = random(logoWidth);
                yRoot = random(logoHeight);
                colorHit = logo.get(
                        (int) (xRoot / logoScale), (int) (yRoot / logoScale));
                if (alpha(colorHit) > 0) {
                    break;
                }
            }
            xRoot += logoOffsetX;
            yRoot += logoOffsetY;
            p.id = particleGlobalId;
            p.rootPos.set(xRoot, yRoot);
            p.loc.set(xRoot, yRoot);
            p.defaultColor = colorHit;
            p.noiseSeed = (float) p.id / 300;

            particleGlobalId += 1;
        }

        for (Swarm s: swarms) {
//            s.configFromParticle();
            s.setEnabled(false);
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
