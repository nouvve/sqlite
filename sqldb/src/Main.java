import java.util.Objects;
import java.util.Scanner;
import java.sql.*;

public class Main {

    static String url = "jdbc:mysql://localhost:3306/1111";
    static String username = "root";
    static String password = "";

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.println("Menedzer bazy danych.");
            System.out.println("[1] - Stworz Tablice");
            System.out.println("[2] - Dodaj rekord");
            System.out.println("[3] - Wyszukaj dane");
            System.out.println("[4] - wyjscie");
            byte wybranaOpcja = s.nextByte();
            s.nextLine();

            switch (wybranaOpcja) {
                case 1: // Tworzenie Tablicy
                    tworzenieTablicy();
                    break;
                case 2: // dodaj rekord
                    dodawanieRekordu();
                    break;
                case 3: // wyszukaj dane
                    wyszukiwanieDanych();
                    break;
                case 4: // wyjdz z programu
                    System.out.println("Wylaczanie.");
                    System.exit(1);
                    break;
                default: // domyslnie, powrot do opcji
                    System.out.println("Niepoprawna opcja, wybierz ponownie");
                    break;
            }

        }
    }

    private static void tworzenieTablicy(){

        Scanner s = new Scanner(System.in);
        System.out.println("Wybierz nazwe nowej tablicy:");
        String nazwa = s.next();
        System.out.println("Ile kolumn powinna miec tablica:");

        int iloscKolumn = s.nextInt();
        String[] nazwyKolumn = new String[iloscKolumn];
        String[] rodzajeKolumn = new String[iloscKolumn];
        boolean[] czyAutoIncrement = new boolean[iloscKolumn];
        String primaryKey;

        for (int i = 0; i<iloscKolumn;i++) {
            System.out.println("UWAGA: pierwsza kolumna jest tez automatycznie kluczem glownym.");
            System.out.println("Podaj nazwe "+(i+1)+" kolumny: ");
            nazwyKolumn[i] = s.next();
            System.out.println("Podaj rodzaj "+(i+1)+" kolumny:");
            System.out.println("liczba | tekst");
            rodzajeKolumn[i] = s.next();
            if (Objects.equals(rodzajeKolumn[i], "liczba")) {
                System.out.println("Ustawiono rodzaj na: liczba");
                rodzajeKolumn[i] = "INT";
            } else if (Objects.equals(rodzajeKolumn[i], "tekst")) {
                System.out.println("Ustawiono rodzaj na: tekst");
                rodzajeKolumn[i] = "TEXT";
            } else {
                System.out.println("nieznaleziono takiej opcji, zmieniono na tekst");
                rodzajeKolumn[i] = "TEXT";
            }
            if (Objects.equals(rodzajeKolumn[i], "INT")) {
                System.out.println("Czy wlaczyc Auto-Increment:");
                System.out.println("[1] - tak");
                System.out.println("[2] - nie");
                int wybor = s.nextInt();
                switch (wybor) {
                    case 1:
                        System.out.println("Wlaczono Auto-Increment");
                        czyAutoIncrement[i] = true;
                        break;
                    case 2:
                        System.out.println("Nie wlaczono Auto-incrmeent");
                        czyAutoIncrement[i] = false;
                        break;
                }
            } else {
                czyAutoIncrement[i] = false;
            }
        }

        String query = "CREATE TABLE "+nazwa+" (";
        for (int i = 0; i<iloscKolumn;i++){
            String pattern;
            if (czyAutoIncrement[i]){
                pattern = nazwyKolumn[i] + " " + rodzajeKolumn[i] + " " + "AUTO_INCREMENT" + ",";
            } else{
                pattern = nazwyKolumn[i] + " " + rodzajeKolumn[i] + ",";
            }
            query = query.concat(pattern);
        }
        String pattern = "PRIMARY KEY ("+nazwyKolumn[0]+"));";
        query = query.concat(pattern); //query koncowe
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            int rowsAffected = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(query);
    }

    private static void dodawanieRekordu(){
        Scanner s = new Scanner(System.in);
        System.out.println("Do jakiej tablicy chcesz dodac rekord: ");
        String nazwa = s.next();
        System.out.println("Tworzenie rekordu dla: "+nazwa);
        String query = "SELECT * FROM "+nazwa;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            String[] wartosci = new String[rsmd.getColumnCount()+1];
            System.out.println("UWAGA: W przypadku wypelniania pola TEXT, dodac \"\".");
            for (int i = 1; i<=rsmd.getColumnCount();i++){
                if (rsmd.isAutoIncrement(i)) continue;
                System.out.println("Wypelnij wartosc dla pola "+rsmd.getColumnName(i)+"("+rsmd.getColumnTypeName(i)+")");
                wartosci[i] = s.next();
                s.nextLine();
            }

            String insert = "INSERT INTO "+nazwa+" VALUES (";
            for (int i = 1; i<rsmd.getColumnCount();i++){
                insert+=(wartosci[i]+", ");
            }
            insert+=(wartosci[rsmd.getColumnCount()]+");");
            System.out.println(insert);
            statement.executeUpdate(insert);
            System.out.println("Pomyslnie dodano rekord.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    private static void wyszukiwanieDanych(){
        Scanner s = new Scanner(System.in);
        System.out.println("W jakiej tablicy chcesz wyszukac dane:");
        String nazwa = s.next();
        System.out.println("Wyszukiwanie w tablicy: "+nazwa);

        System.out.println("Wyszukiwanie konkretne, czy wyswietlanie calosci:");
        System.out.println("[1] - Wyszukiwanie konkretne");
        System.out.println("[2] - Wyswietlenie calej tablicy");
        int wybor = s.nextInt();
        switch (wybor){
            case 1:
                System.out.println("Kolumna do przeszukania");
                String kolumna = s.next();
                System.out.println("UWAGA: Jezeli jest to text, trzeba dodac \"\"");
                System.out.println("Wartosc szukana:");
                String warunek = s.next();

                String query = "SELECT * FROM "+nazwa+" WHERE "+kolumna+"="+warunek;
                try (Connection connection = DriverManager.getConnection(url, username, password);
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    ResultSet resultSet = statement.executeQuery(query);
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    int liczbaKolumn = rsmd.getColumnCount();

                    while (resultSet.next()){
                        for (int i = 1; i<= liczbaKolumn;i++){
                            System.out.print(resultSet.getString(i) + " ");
                        }
                        System.out.println();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                String queryw = "SELECT * FROM "+nazwa;
                try (Connection connection = DriverManager.getConnection(url, username, password);
                     PreparedStatement statement = connection.prepareStatement(queryw)) {
                     ResultSet resultSet = statement.executeQuery(queryw);
                     ResultSetMetaData rsmd = resultSet.getMetaData();
                     int liczbaKolumn = rsmd.getColumnCount();

                     while (resultSet.next()){
                         for (int i = 1; i<= liczbaKolumn;i++){
                             System.out.print(resultSet.getString(i) + " ");
                         }
                         System.out.println();
                     }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Niepoprawna opcja, cofanie");
                break;
        }
    }
}