import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SymulacjaLaki extends JFrame {
    private PanelLaki panelLaki;

    public SymulacjaLaki(int liczbaPszczol, int poczatkowaPredkosc, int liczbaGasienic, int poczatkowaPredkoscGasienic) {
        setTitle("Symulacja Łąki");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        panelLaki = new PanelLaki(liczbaPszczol, poczatkowaPredkosc, liczbaGasienic, poczatkowaPredkoscGasienic);
        panelLaki.setBounds(0, 0, 800, 600); //stały rozmiar i położenie panelu w obrębie ramki
        add(panelLaki);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Podaj liczbę pszczół: ");
        int liczbaPszczol = scanner.nextInt();

        System.out.print("Podaj początkową prędkość pszczół: ");
        int poczatkowaPredkosc = scanner.nextInt();

        System.out.print("Podaj liczbę gąsienic: ");
        int liczbaGasienic = scanner.nextInt();

        System.out.print("Podaj początkową prędkość gąsienic: ");
        int poczatkowaPredkoscGasienic = scanner.nextInt();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SymulacjaLaki symulacja = new SymulacjaLaki(liczbaPszczol, poczatkowaPredkosc, liczbaGasienic, poczatkowaPredkoscGasienic);
                symulacja.setVisible(true);
            }
        });
    }
}

class PanelLaki extends JPanel {
    private ArrayList<Obiekt> obiekty;
    private boolean uruchomiony;
    private int liczbaPszczol;
    private int predkoscPszczol;
    private int liczbaGasienic;
    private int predkoscGasienic;

    public PanelLaki(int liczbaPszczol, int predkoscPszczol, int liczbaGasienic, int predkoscGasienic) {
        this.liczbaPszczol = liczbaPszczol;
        this.predkoscPszczol = predkoscPszczol;
        this.liczbaGasienic = liczbaGasienic;
        this.predkoscGasienic = predkoscGasienic;
        obiekty = new ArrayList<>();//lista z pszczołami, kwiatkami i gąsienicami
        setBackground(new Color(75, 166, 149));
        setPreferredSize(new Dimension(800, 600)); // preferowany rozmiar panelu
        uruchomSymulacje(liczbaPszczol, predkoscPszczol, liczbaGasienic, predkoscGasienic);
    }

    public void uruchomSymulacje(int liczbaPszczol, int predkoscPszczol, int liczbaGasienic, int predkoscGasienic) {
        obiekty.clear();
        uruchomiony = true;

        Random random = new Random();

        //ustawienie początkowych pozycji pszczół i gąsienic po ustawieniu rozmiaru panelu
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < liczbaPszczol; i++) {
                    Pszczola pszczola = new Pszczola(random.nextInt(getWidth()), random.nextInt(getHeight()), predkoscPszczol);
                    obiekty.add(pszczola);
                    new Thread(pszczola).start();//WĄTEK PSZCZOŁA
                }

                for (int i = 0; i < liczbaGasienic; i++) {
                    Gasienica gasienica = new Gasienica(random.nextInt(getWidth()), random.nextInt(getHeight()), predkoscGasienic);
                    obiekty.add(gasienica);
                    new Thread(gasienica).start();//WĄTEK GĄSIENICA
                }
            }
        });

        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (uruchomiony) {
                    repaint();
                }
            }
        });
        timer.start();

        Timer kwiatekTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (uruchomiony) {
                    ArrayList<Pszczola> pszczoly = new ArrayList<>();//tymczasowa lista pszczółek
                    for (Obiekt obiekt : obiekty) {
                        if (obiekt instanceof Pszczola) {
                            pszczoly.add((Pszczola) obiekt);
                        }
                    }//utworzenie listy pszczółek (tymczasowej)
                    if (!pszczoly.isEmpty()) {//jeśli lista pszczół nie jest pusta: czyli istnieją pszczoły
                        Pszczola randomPszczola = pszczoly.get(random.nextInt(pszczoly.size()));//wybierana jest losowa pszczoła z tymczasowej listy pszczół
                        Kwiatek kwiatek = new Kwiatek(randomPszczola.x, randomPszczola.y);//rysuje kwaitek w miejsce randomowej pszczoły
                        obiekty.add(kwiatek);//do listy obiektów dodawany jest kwiatek
                    }
                }
            }
        });//koniec klasy wewnętrznej (która jest parametrem obiektu kwiatekTimer)
        kwiatekTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Obiekt obiekt : obiekty) {
            obiekt.rysuj(g);
        }

        ArrayList<Obiekt> toRemove = new ArrayList<>();
        for (Obiekt obiekt : obiekty) {
            if (obiekt instanceof Gasienica) {
                Gasienica gasienica = (Gasienica) obiekt;
                for (Obiekt inny : obiekty) {
                    if (inny instanceof Kwiatek && gasienica.czyDotknal(inny)) {
                        toRemove.add(inny);
                    } else if (inny instanceof Pszczola && gasienica.czyDotknal(inny)) {
                        ((Pszczola) inny).spowolnij();//spowolnij() jest w klasie pszczoła
                    }
                }
            }
        }
        obiekty.removeAll(toRemove);//usuwanie kwiatka (który jest na liście toRemove)
    }
}

abstract class Obiekt implements Runnable {
    protected int x, y, predkosc;
    protected int szerokosc = 20, wysokosc = 20;
    protected Random random = new Random();
    protected int kierunekX = random.nextBoolean() ? 1 : -1;
    protected int kierunekY = random.nextBoolean() ? 1 : -1;

    public Obiekt(int x, int y, int predkosc) {
        this.x = x;
        this.y = y;
        this.predkosc = predkosc;
    }

    public void rusz() {//zasady poruszania się obiektu
        if (random.nextDouble() < 0.1) {
            kierunekX = random.nextBoolean() ? 1 : -1;//losowa zmiana kierunku w osi X
            kierunekY = random.nextBoolean() ? 1 : -1;//losowa zmiana kierunku w osi y
        }
        x += kierunekX * predkosc;
        y += kierunekY * predkosc;

        if (x < 0 || x > 780) kierunekX *= -1;//warunek na odbijanie się od ścianek panelu
        if (y < 0 || y > 580) kierunekY *= -1;
    }

    @Override
    public void run() {
        while (true) {
            rusz();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public abstract void rysuj(Graphics g);

    public boolean czyDotknal(Obiekt inny) {
        return x < inny.x + inny.szerokosc && x + szerokosc > inny.x && y < inny.y + inny.wysokosc && y + wysokosc > inny.y;//warunek na false albo true
    }
}

class Pszczola extends Obiekt {
    public Pszczola(int x, int y, int predkosc) {
        super(x, y, predkosc);
    }

    @Override
    public void rysuj(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, szerokosc, wysokosc);
    }

    public void spowolnij() {
        if (predkosc > 1) {
            predkosc--;
        }
    }
}

class Kwiatek extends Obiekt {
    public Kwiatek(int x, int y) {
        super(x, y, 0);
    }

    @Override
    public void rysuj(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, szerokosc, wysokosc);
    }

    @Override
    public void run() {
    }
}

class Gasienica extends Obiekt {
    public Gasienica(int x, int y, int predkosc) {
        super(x, y, predkosc);
    }

    @Override
    public void rysuj(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, szerokosc, wysokosc);
    }
}
