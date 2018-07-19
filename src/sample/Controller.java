package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller implements Initializable {

    public TextField skaiciusNuoTextField;
    @FXML
    public TextField skaiciusIkiTextField;
    @FXML
    public TextField zingsnisTextField;
    @FXML
    public Label pastabosLabel;
    @FXML
    public Label procentaiLabel;
    @FXML
    public Button buttonButton;
    @FXML
    private ProgressBar progresoJuostaProgressBar;

    private long MIN_PAUSE = 500; // užlaikymas milisekundėmis 0,5sec
    private String SPLITER = "---"; // eilutės skaldymo skyriklis
    protected static LinkedList<String> irasaiTreeSet = new LinkedList<>();
    private TreeSet<Integer> pirminiuSkaiciuKolekcijaTreeSet = new TreeSet<>(); // galima apsieti be static
    private boolean isStoped = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");

    private int gijuSkaitliukas = 0;
    private boolean nutrauktiSkaiciavimus = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pirminiuSkaiciuKolekcijaTreeSet.add(2);
    }

    // [Pradėti] mygtukas
    public void pradeti() {

        // kai: 0 skaičiavimas nevyksta, 1 skaičiuojama, 2 laukia eilėje, > 2 atmetama
        gijuSkaitliukas++;

        // gijų skaičiaus apribojimas iki 2
        if (gijuSkaitliukas > 2) {
            gijuSkaitliukas--;
            return;
        }

        // antrosios gijos stabdymas iki kol bus nutraukti skaičiavimai
        if (gijuSkaitliukas == 2) {
            nutrauktiSkaiciavimus = true;
            while (nutrauktiSkaiciavimus) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            gijuSkaitliukas = 1;
        }

        isStoped = false;

        // skaičių įvedimas ir įvedimo klaidų tikrinimas
        int numberNuo, numberIki, numberZingsnis;
        try {
            numberNuo = Integer.parseInt(skaiciusNuoTextField.getText());
            numberIki = Integer.parseInt(skaiciusIkiTextField.getText());
            numberZingsnis = Integer.parseInt(zingsnisTextField.getText());
            pastabosLabel.setText("");
        } catch (NumberFormatException e) {
            pastabosLabel.setText("Įrašytas ne skaičius. Prašome pataisyti.");
            gijuSkaitliukas = 0;
            return;
        }
        if (numberNuo < 2 || numberIki < numberNuo || numberZingsnis < 1) {
            pastabosLabel.setText("Prašome pataisyti.\n\n" +
                    "Pirmasis skaičius turi būti didesnis už vienetą.\n" +
                    "Antrasis skaičius turi būti didesnis arba lygus pirmajam.\n" +
                    "Trečiasis skaičius turi būti didesnis už nulį.");
            gijuSkaitliukas = 0;
            return;
        }


        // įrašų ir progress bar valdymo nustatymai
        Task progressTask = pagrindinis(numberNuo, numberIki, numberZingsnis);
        progresoJuostaProgressBar.progressProperty().unbind();
        progresoJuostaProgressBar.progressProperty().bind(progressTask.progressProperty());
        progressTask.messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String m[] = newValue.split(SPLITER);
                procentaiLabel.setText(m[0]);
                pastabosLabel.setText(m[1]);
            }
        });

        // naujų gijų sukūrimas skaičiavimams
        Thread gija = new Thread(progressTask);
        gija.start();
    }

    private Task pagrindinis(int numberNuo, int numberIki, int numberZingsnis) {
        return new Task() {
            @Override
            public Object call() throws Exception {
                updateProgress(0, 1);

                // skaidomų skaičių masyvas
                TreeSet<Integer> skaidomiSkaiciaiTreeSet = new TreeSet<>();

                // skaidomų skaičių radimas ir patalpinimas į masyvą
                for (int i = numberNuo; i <= numberIki; i += numberZingsnis) {

                    skaidomiSkaiciaiTreeSet.add(i);

                    // skaičiavimų nutraukimas pradėjus naują skaičių skaidymą
                    if (nutrauktiSkaiciavimus) {
                        nutrauktiSkaiciavimus = false;
                        return null;
                    }
                }

                // laiko formatas
                dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

                // pirmasis įrašas su [.] tarp sec - millisec
                irasaiTreeSet.add(dateFormat.format(new Date()) +
                        " Skaičiavimo pradžia. Naudojami skaičiai: " +
                        numberNuo + ", " + numberIki + ", " + numberZingsnis + ".");

                // likusių eilučių formavimas su FOR (pagrindinis ciklas)
                boolean isDot = true;
                int progress = 0;

                // pagrindinis programos ciklas, skirtas eilučių sukūrimui ir įrašymui į TreeSet masyvą
                for (int skaicius : skaidomiSkaiciaiTreeSet) {

                    // naujos eilutės pradžios laikas
                    long laikasPries = System.currentTimeMillis();

                    // label įrašų sukūrimas, String eilutės struktūra:
                    // progreso juostos skaitinė reikšmė proc, "---" spliter, pastabos apie einamajį skaidomą skaičių
                    updateMessage(progress * 100 / skaidomiSkaiciaiTreeSet.size() + "  %" +
                            SPLITER + "Skaidomas skaičius: " + skaicius);
                    // todo: pakeisti String eilutę su SPLITER į String masyvas[2]

                    // įrašo sudarymas ir priskyrimas masyvui
                    String irasas = dateFormat.format(laikasPries) + " " +
                            skaicius + "=" + rastiPirminiusDauginamuosius(skaicius);
                    irasaiTreeSet.add(irasas);

                    // skaičiavimų nutraukimas pradėjus naują skaičių skaidymą
                    if (nutrauktiSkaiciavimus) {
                        irasaiTreeSet.removeLast(); // paskutinės eilutės išmetimas iš sąrašo, nes eilutė nebaigta kurti
                        nutrauktiSkaiciavimus = false;
                        return null;
                    }

                    // pauzė
                    while (System.currentTimeMillis() < laikasPries + MIN_PAUSE && !nutrauktiSkaiciavimus) {
                        Thread.sleep(1);
                    }

                    // formato keitimas trečiajai ir likusioms eilutėms: [.] -> [:] tarp sec - millisec
                    if (isDot) {
                        dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
                        isDot = false;
                    }

                    // progreso juostos atnaujinimas
                    progress++;
                    updateProgress(progress, skaidomiSkaiciaiTreeSet.size());


                    // label įrašų sukūrimas, String eilutės struktūra:
                    // progreso juostos skaitinė reikšmė proc, "---" spliter, pastabos apie baigtą skaidymą
                    // reikalingas papildomas atnaujinimas paskutiniam veiksmui su įrašais
                    if (skaicius == skaidomiSkaiciaiTreeSet.last() || isStoped) {
                        updateMessage(progress * 100 / skaidomiSkaiciaiTreeSet.size() + "  %" +
                                SPLITER + "Skaidymas baigtas. Rezultatai faile " + WriteData.FILE);
                    }

                    // veiksmai paspaudus button [Baigti]
                    if (isStoped) {
                        isStoped = false;
                        WriteData.writeData(irasaiTreeSet);
                        break;
                    }
                } // end for

                // paskutiniosios eilutės formavimas ir įrašymas į masyvą
                irasaiTreeSet.add(dateFormat.format(new Date()) + " Skaičiavimo pabaiga.");
                WriteData.writeData(irasaiTreeSet);

                // kintamųjų apnulinimai pilnai užbaigus skaidymą
                gijuSkaitliukas = 0;
                nutrauktiSkaiciavimus = false;

                return null;
            } //--------------------------------------
        };
    }


    // button [Baigti] vygdomasis metodas
    public void baigti(ActionEvent event) {
        isStoped = true;
        gijuSkaitliukas = 0;
        nutrauktiSkaiciavimus = false;
        WriteData.writeData(irasaiTreeSet);
        pastabosLabel.setText("Skaidymas baigtas. Rezultatai faile " + WriteData.FILE);
    }


    // pirminių skaičių dauginamųjų paieškos metodas
    private String rastiPirminiusDauginamuosius(int iki) {

        // programos inicializacijos metu pirminių skaičių sekai priskiriama 2
        papildytiPirminiuSkaiciuKolekcija(iki);
        // todo: pirminių skaičių kolekciją papildyti tik esant būtinybei:
        // todo: kai paimamams paskutinis pirminis skaičiaus kolekcijoje ir reikalingas sekantis

        int pirminis2 = 0; // skirtas išsinešti reikšmei už FOR ciklo ribų

        // pirminių skaičių eilutės lipdymas (2*2*2...)
        String stringas = ""; // grąžinama eilutė
        while (true) {

            // tikrina visus pirminius iš eilės
            for (int pirminis : pirminiuSkaiciuKolekcijaTreeSet) {

                // skaičiavimų nutraukimas pradėjus naują skaičių skaidymą
                if (nutrauktiSkaiciavimus) {
                    return "";
                }

                pirminis2 = pirminis;

                if (iki % pirminis == 0) {
                    stringas += pirminis + "*";
                    break; // išeina iš for kai randa pirminį
                }
            }
            if (iki == pirminis2) {
                break; // nutraukia amžina ciklą kai randa paskutinį pirminį skaičių
            }
            iki /= pirminis2;
        }

        // pašalinamas paskutinis simbolis '*'
        stringas = stringas.substring(0, stringas.length() - 1);

        return stringas;
    }


    // pirminių skaičių kolekcijos papildymas
    private void papildytiPirminiuSkaiciuKolekcija(int iki) {
        boolean isPrime = true;
        int nuo = pirminiuSkaiciuKolekcijaTreeSet.last() + 1;
        while (nuo <= iki) {
            // pirminių skaičių paieškos algoritmas
            for (int j = 2; j <= Math.sqrt(nuo); j++) {

                // skaičiavimų nutraukimas pradėjus naują skaičių skaidymą
                if (nutrauktiSkaiciavimus) {
                    return;
                }

                if (nuo % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime) {
                pirminiuSkaiciuKolekcijaTreeSet.add(nuo);
            }
            isPrime = true;
            nuo++;
        }
    }
}
