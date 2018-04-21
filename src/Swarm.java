import java.util.ArrayList;

public class Swarm extends Movable {
    private int size;
    Particle[] particles;

    Swarm(int id, int size, boolean createParticles) {
        super(id);
        this.size = size;
        if (createParticles) {
            this.createParticles();
        }
    }

    @Override
    void update() {
        for (Particle p: particles) {
            p.update();
        }
    }

    @Override
    void render() {
        for (Particle p: particles) {
            p.render();
        }
    }

    // create particles with current size
    private void createParticles() {
        particles = new Particle[size];
        for (int i = 0; i < size; i += 1) {
            particles[i] = new Particle();
            particles[i].swarm = this;
        }
    }

    int getSize() {
        return size;
    }
}
