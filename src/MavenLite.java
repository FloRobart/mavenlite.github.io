//!/usr/bin/env java

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * MavenLite est un programme qui permet de compiler et de lancer un projet java
 * @author Floris Robart
 * @version 2.0.0
 * @since 1.0.0
 * @see <a href="https://github.com/FloRobart/Maven_lite/blob/master/maven_lite.sh">maven-lite</a>
 */
public class MavenLite 
{
    /*===================================*/
    /* Constantes et valeurs par défauts */
    /*===================================*/
    private static final String PROJECT_NAME    = System.getProperty("user.dir").substring(System.getProperty("user.dir").lastIndexOf(File.separator)+1);
    private static final String AUTHOR          = "Floris Robart";
    private static final String VERSION         = "2.0.0";
    private static final String ENCODING        = "UTF-8";
    private static final String EXPORT          = "run.java";
    private static final String OUTPUT          = "bin"; // TODO : L'output doit être modifier quand les options -cr ou -s sont utilisées
    private static final String CREATE          = "TestApp";
    private static final String FILE            = "LPOM";
    private static final String SOURCE          = "src" + File.separator + "main" + File.separator + "java";
    private static final String LIBRARIES       = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "lib";
    private static final String MAIN            = "App";
    private static final String ROOT_PROJECT    = ".";

    private static final String COMPILE_LIST_NAME = "compile.list";

    /* Couleurs et style */
    private static final String DEFAULT         = "\u001B[0m";
    private static final String GREEN_BOLD      = "\u001B[1;32m";
    private static final String FULL_GREEN_BOLD = "\u001B[38;5;46;1m";
    private static final String BOLD            = "\u001B[1m";
    private static final String RED_BOLD        = "\u001B[1;31m";
    private static final String BLUE_BOLD       = "\u001B[1;34m";
    private static final String YELLOW_BOLD     = "\u001B[1;33m";

    /* Messages */
    private static final String SUCCESS         = "[" + MavenLite.FULL_GREEN_BOLD + "SUCCESS" + MavenLite.DEFAULT + "] ";
    private static final String INFO            = "[" + MavenLite.BLUE_BOLD + "INFO" + MavenLite.DEFAULT + "] ";
    private static final String WARNING         = "[" + MavenLite.YELLOW_BOLD + "WARNING" + MavenLite.DEFAULT + "] ";
    private static final String ERROR           = "[" + MavenLite.RED_BOLD + "ERROR" + MavenLite.DEFAULT + "] ";


    /*===========*/
    /* Variables */
    /*===========*/
    /* Parse et exécution des options */
    private List<String[]> lstOptions;
    private Map<String, String> hmArgs;
    private int countArgs = 1;



    /*==============*/
    /* Constructeur */
    /*==============*/
    /**
     * Coeur du programme
     * @param args les arguments passés au programme
     */
    private MavenLite(String[] args)
    {
        this.lstOptions = this.initOptions();
        this.hmArgs = new HashMap<String, String>();

        /* Ajout des options obligatoire dans la hashmap */
        for (String[] opt : this.lstOptions)
            if (opt[6].equals("1"))
                this.hmArgs.put(opt[0], opt[5]);

        this.parseOptions(args);
        this.executeOption(this.lstOptions);
    }

    /**
     * Constructeur qui permet uniquement de charger la liste des options.
     */
    private MavenLite()
    {
        this.lstOptions = this.initOptions();
    }



    /*==========================================*/
    /* Méthodes pour la préparation des options */
    /*==========================================*/
    /**
     * Initialise la liste des options.
     * <br />
     * Les options sont sous la forme d'un tableau de String.
     * <ul>
     *   <li>0 : nom de l'option</li>
     *   <li>1 : nom court de l'option</li>
     *   <li>2 : nom long de l'option</li>
     *   <li>3 : nombre d'argument de l'option minimum</li>
     *   <li>4 : nombre d'argument de l'option maximum</li>
     *   <li>5 : valeur par défaut de l'option</li>
     *   <li>6 : numéro d'ordonnancement de l'option</li>
     *   <li>7 : description de l'option</li>
     * </ul>
     * @return la liste des options possible
     */
    private List<String[]> initOptions()
    {
        List<String[]> lst = new ArrayList<String[]>();

        /*                             nom                  court    long                       nb argument  nb argument  valeur                  utilisé    description       */
        /*                             0                    1        2                          3            4            5                       6          7                 */
        /* 0  */ lst.add(new String[] {"file"             , "-f"   , "--file"                 , "0"        , "1"        , MavenLite.FILE        , "0", "Fichier de configuration. Permet de charger les options à partir d'un fichier de configuration, le séparateur sont l'espace et le retour à la ligne. Les options du fichier de configuration prédomine sur les options de la ligne de commande. L'option -f doit obligatoirement être la première option de la ligne de commande."});
        /* 1  */ lst.add(new String[] {"create"           , "-cr"  , "--create"               , "0"        , "1"        , MavenLite.CREATE      , "0", "Créer l'arborescence du projet ainsi qu'un fichier de config par défaut. Si le dossier de sortie n'est pas spécifié, le dossier par défaut est le dossier courant."});
        /* 2  */ lst.add(new String[] {"compilation"      , "-c"   , "--compilation"          , "0"        , "0"        , null                  , "0", "Compiler le projet."});
        /* 3  */ lst.add(new String[] {"launch"           , "-l"   , "--launch"               , "0"        , "0"        , null                  , "0", "Lancer le projet."});
        /* 4  */ lst.add(new String[] {"compile-launch"   , "-cl"  , "--compile-launch"       , "0"        , "0"        , null                  , "0", "Compiler et lancer le projet. (équivalent à -c -l)"});
        /* 5  */ lst.add(new String[] {"launch-compile"   , "-lc"  , "--launch-compile"       , "0"        , "0"        , null                  , "0", "Compiler et lancer le projet. (équivalent à -c -l)"});
        /* 6  */ lst.add(new String[] {"mvc"              , "-mvc" , "--model-view-controller", "0"        , "0"        , null                  , "0", "Permet de créer l'arborescence d'un projet MVC."});
        /* 7  */ lst.add(new String[] {"quiet"            , "-q"   , "--quiet"                , "0"        , "0"        , null                  , "0", "Créer l'arborescence du projet ainsi qu'un fichier de config par défaut. Si le dossier de sortie n'est pas spécifié, le dossier par défaut est le dossier courant."});
        /*^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^*/
        /* Tous se qui est au dessus de cette ligne ne doit pas être déplacer, leur ordre est important */
        
        /*^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^*/
        /* Tous se qui est au dessus de cette ligne ne doit pas être déplacer en dehors des commentaires qui les entours mais peux être interchanger entre eux */
        /* 8  */ lst.add(new String[] {"verbose"          , "-v"   , "--verbose"              , "0"        , "0"        , null                  , "0", "Permet d'afficher les commandes exécutées."});
        /* 9  */ lst.add(new String[] {"maven"            , "-mvn" , "--maven"                , "0"        , "0"        , null                  , "0", "Convertir le projet en projet maven."});
        /* 10 */ lst.add(new String[] {"export"           , "-ex"  , "--export"               , "0"        , "1"        , MavenLite.EXPORT      , "0", "Exporter le projet dans un fichier jar. Le fichier jar sera exporté dans le dossier de sortie. Le nom du fichier jar est le nom du dossier de sortie."});
        /* 11 */ lst.add(new String[] {"source"           , "-s"   , "--source"               , "1"        , "1"        , MavenLite.SOURCE      , "1", "Dossier racine du projet à compiler."});
        /* 12 */ lst.add(new String[] {"output"           , "-o"   , "--output"               , "1"        , "1"        , MavenLite.OUTPUT      , "1", "Dossier de sortie des fichiers compilés."});
        /* 13 */ lst.add(new String[] {"classpath"        , "-cp"  , "--classpath"            , "1"        , "1"        , null                  , "0", "Liste des fichiers jar et du dossier de sortie des fichiers compilés (le même dossier que pour l'option -o) à ajouter au classpath lors de la compilation et du lancement. Les fichiers jar doivent être séparés par des ':'. La valeur par defaut du classpath est le dossier de sortie des fichiers compilés si l'option -o est utilisé, sinon le classpath sera le dossier courent."});
        /* 14 */ lst.add(new String[] {"libraries"        , "-lib" , "--libraries"            , "0"        , "1"        , MavenLite.LIBRARIES   , "0", "Dossier contenant les fichiers jar utiliser par le programme. Tout les fichiers jar seront ajoutés au classpath lors de la compilation et du lancement."});
        /* 15 */ lst.add(new String[] {"encoding"         , "-e"   , "--encoding"             , "1"        , "1"        , MavenLite.ENCODING    , "1", "Permet de changer l'encodage des fichiers java à compiler. L'encodage par defaut est 'UTF-8'. Utilisable uniquement avec l'option -c."});
        /* 16 */ lst.add(new String[] {"main"             , "-m"   , "--main"                 , "1"        , "1"        , null                  , "0", "Classe principale à lancer. Utilisable uniquement avec l'option -l."});
        /* 17 */ lst.add(new String[] {"argument"         , "-arg" , "--argument"             , "1"        , "1"        , null                  , "0", "Argument unique"});
        /* 18 */ lst.add(new String[] {"arguments"        , "-args", "--arguments"            , "unlimited", "unlimited", null                  , "0", "Arguments à passer à la classe principale. Si vous voulez passé un argument qui commencer par '-' parce qu'aucune erreur ne sera déclancher, il faut échapper le caractère '-' avec deux '\\' comme ceci : '-args \\\\-argument_pour_le_main'."});
        /* 19 */ lst.add(new String[] {"version"          , "-V"   , "--version"              , "0"        , "0"        , MavenLite.VERSION     , "0", "Afficher la version et quitter."});
        /* 20 */ lst.add(new String[] {"help"             , "-h"   , "--help"                 , "0"        , "0"        , null                  , "0", "afficher l'aide et quitter."});
        /* Pour ajouter une option, il faut ajouter un tableau de String dans la liste lstOptions et ajouter l'option dans le switch de la méthode executeOption(). Il peut être nécessaire d'ajouter une méthode pour l'option. */
        /* 21 */ lst.add(new String[] {"test"             , "-t"   , "--test"                 , "0"        , "0"        , null                  , "0", "Permet de tester des méhodes."});

        return lst;
    }


    /**
     * Parse les arguments passés au programme.
     * Si une option est passée plusieurs fois, seule la dernière sera prise en compte.
     * @param args les arguments à parser 
     */
    private void parseOptions(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            System.out.println("args[" + i + "] = " + args[i]); // debug
            boolean bFound = false;

            /* Si l'option est "-f" ou "--file. Ne fonctionne qu'une seul fois */
            if ((args[i].equals(this.lstOptions.get(0)[1]) || args[i].equals(this.lstOptions.get(0)[2])))
            {
                if (!this.lstOptions.get(0)[6].equals("0"))
                {
                    if ((i+1 < args.length) && !args[i+1].startsWith("-"))
                        i++;

                    continue;
                }

                if ((i+1 < args.length) && !args[i+1].startsWith("-"))
                    this.hmArgs.put(this.lstOptions.get(0)[0], args[++i]);
                else
                    this.hmArgs.put(this.lstOptions.get(0)[0], this.lstOptions.get(0)[5]);

                this.lstOptions.get(0)[6] = String.valueOf(1);
                this.file(this.hmArgs.get(this.lstOptions.get(0)[0]));

                bFound = true;
            }

            for (String[] opt : this.lstOptions)
            {
                if (bFound) break;

                if (args[i].equals(opt[1]) || args[i].equals(opt[2]))
                {
                    /* Parse les options qui ont sois 0 sois 1 seul argument */
                    if (opt[3].equals("0"))
                    {
                        if (opt[4].equals("1") && (i+1 < args.length) && !args[i+1].startsWith("-"))
                            this.hmArgs.put(opt[0], args[++i]);
                        else if (opt[4].equals("0") && opt[5] == null)
                            this.hmArgs.put(opt[0], "true");
                    }
                    /* Parse les options qui ont obligatoirement 1 seul argument */
                    else if (opt[3].equals("1"))
                    {
                        if (i+1 < args.length && !args[i+1].startsWith("-"))
                            this.hmArgs.put(opt[0], args[++i].replace("\\", ""));
                        else
                        {
                            System.out.println(MavenLite.WARNING + "L'option '" + MavenLite.BLUE_BOLD + args[i] + MavenLite.DEFAULT + "' nécessite un argument.");

                            if (i+1 < args.length && args[i+1].startsWith("-"))
                                System.out.println(MavenLite.WARNING + "Si l'argument comment par le caractère '-' veuillez échapé le caractère '-' avec deux '\\' comme ceci : '" + MavenLite.BLUE_BOLD + args[i] + " \\\\" + args[i+1] + MavenLite.DEFAULT + "'");

                            System.exit(0);
                        }
                    }
                    /* Parse les options qui peuvent avoir un nombre illimité d'argument */
                    else if (opt[3].equals("unlimited"))
                    {
                        StringBuilder sArgs = new StringBuilder();
                        sArgs.append(this.hmArgs.get(opt[0]) == null ? "" : this.hmArgs.get(opt[0]));

                        while (i + 1 < args.length && !args[i + 1].startsWith("-"))
                            sArgs.append("\"").append(args[++i].replace("\\", "")).append("\";");

                        this.hmArgs.put(opt[0], sArgs.toString());
                    }
                    /* Parse les options qui ont un nombre d'argument défini autre que 0 et 1 */
                    /* Pour l'instant ce code n'est pas utilisé */
                    else
                    {
                        StringBuilder sArgs = new StringBuilder();
                        sArgs.append(this.hmArgs.get(opt[0]) == null ? "" : this.hmArgs.get(opt[0]));

                        for (int j = 0; j < Integer.parseInt(opt[3]) && i+1 < args.length && !args[i + 1].startsWith("-"); j++)
                            sArgs.append("\"").append(args[++i].replace("\\", "")).append("\";");

                        this.hmArgs.put(opt[0], sArgs.toString());

                        // Je ne sais pas pourquoi j'ai fait ça
                        // for (int j = i-Integer.parseInt(opt[3]); j < i+Integer.parseInt(opt[3]) && i+1 < args.length; j++)
                        //     args = this.removeTabElement(args, i);
                    }

                    opt[6] = String.valueOf(this.countArgs);
                    bFound = true;
                    break;
                }
            }

            if (!bFound)
            {
                System.out.println(MavenLite.ERROR + "Option '" + args[i] + "' inconnue !");
                System.exit(1);
            }
            else
            {
                this.countArgs++;
            }
        }
    }


    /**
     * Trie la liste des options dans l'ordre de passage à la ligne de commande et supprime les options non utilisées
     * @param lst la liste des options à trier
     * @return la liste des options triées
     */
    private List<String[]> sortList(List<String[]> lst)
    {
        List<String[]> lstSorted = new ArrayList<String[]>(lst.size());

        for (int i = 1; i < lst.size(); i++)
            for (int j = 0; j < lst.size(); j++)
                if (lst.get(j)[6].equals(String.valueOf(i)))
                    lstSorted.add(lst.get(j));

        for (int i = 0; i < lst.size(); i++)
            if (lst.get(i)[6].equals("0"))
                lstSorted.add(lst.get(i));

        return lstSorted;
    }


    /**
     * Trie la liste des options dans l'ordre de passage à la ligne de commande et supprime les options non utilisées
     * @param lst
     * @return
     */
    private List<String[]> removeUnusedOption(List<String[]> lst)
    {
        List<String[]> lstSorted = new ArrayList<String[]>();

        for (String[] opt : lst)
            if (!opt[6].equals("0"))
                lstSorted.add(opt);

        return lstSorted;
    }


    /**
     * Supprime un élément d'un tableau
     * @param tab le tableau à modifier
     * @param index l'index de l'élément à supprimer
     * @return le tableau modifié
     */
    private String[] removeTabElement(String[] tab, int index)
    {
        String[] newTab = new String[tab.length-1];

        for (int i = 0; i < tab.length; i++)
            if (i < index)
                newTab[i] = tab[i];
            else if (i > index)
                newTab[i-1] = tab[i];
        
        return newTab;
    }



    /*===================*/
    /* Méthodes générale */
    /*===================*/
    /**
     * Permet de détecter automatiquement le fichier main à lancer
     * @param f le dossier à parcourir
     * @return le nom de la classe principale
     */
    private String getMainClassName(File directory)
    {
        String mainClass = "";

        if (directory.exists() && directory.isDirectory())
        {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".java"))
                    {
                        try
                        {
                            Scanner scanner = new Scanner(file);
                            while (scanner.hasNextLine())
                            {
                                String line = scanner.nextLine();
                                if (line.replaceAll(" ", "").contains("public static void main(String[]".replaceAll(" ", "")))
                                {
                                    /* Récupération du package */
                                    mainClass = this.getPackageName(file) + file.getName().replace(".java", "");
                                    break;
                                }
                            }
                            scanner.close();
                        }
                        catch (IOException e)
                        {
                            System.out.println(MavenLite.ERROR + "La lecture du fichier '" + MavenLite.BLUE_BOLD + file.getName() + MavenLite.DEFAULT + "' a échoué.");
                            System.exit(1);
                        }
                    }
                    else if (file.isDirectory())
                    {
                        String subdirectoryMainClass = getMainClassName(file);
                        if (!subdirectoryMainClass.isEmpty())
                        {
                            mainClass = subdirectoryMainClass;
                            break;
                        }
                    }
                }
            }
        }
        else
        {
            System.out.println(MavenLite.ERROR + "Le fichier '" + directory.getName() + "' n'existe pas ou n'est pas un dossier.");
            System.exit(1);
        }

        System.out.println("Main Class = " + mainClass);
        return mainClass;
    }

    /**
     * Permet de récupérer le nom du package d'un fichier java
     * @param javaFile le fichier java à analyser
     * @return le nom du package au format "package.name."
     */
    private String getPackageName(File javaFile)
    {
        String packageName = "";

        try
        {
            Scanner scanner = new Scanner(javaFile);
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine().replaceAll("^[\\u0009\\u0020]*", "");
                if (line.contains("package"))
                {
                    packageName = line.split(" ")[1].replace(";", ".");
                    break;
                }
                else if (!line.replaceAll("^[\\u0009\\u0020]*", "").equals(""))
                {
                    break;
                }
            }
            scanner.close();
        }
        catch (IOException e)
        {
            System.out.println(MavenLite.ERROR + "La lecture du fichier '" + javaFile.getName() + "' a échoué.");
            System.exit(1);
        }

        return packageName;
    }


    /**
     * Génère la liste des fichiers à compiler
     * @param source le dossier à parcourir
     * @return la liste des fichiers à compiler
     */
    private String genererCompileList(File source)
    {
        StringBuilder sCompileList = new StringBuilder();
    
        if (source.exists() && source.isDirectory())
        {
            File[] lstFiles = source.listFiles();
            if (lstFiles != null)
            {
                for (File file : lstFiles)
                {
                    if (file.isFile() && file.getName().endsWith(".java"))
                    {
                        sCompileList.append(file.getPath()).append("\n");
                    }
                    else if (file.isDirectory())
                    {
                        sCompileList.append(this.genererCompileList(file));
                    }
                }
            }
        }
        else
        {
            if (source.isFile())
                System.out.println(MavenLite.ERROR + "Le fichier '" + source + "' n'est pas un dossier !");
            else
                System.out.println(MavenLite.ERROR + "Le dossier '" + source + "' n'existe pas !");

            System.exit(1);
        }

        return sCompileList.toString();
    }


    /**
     * Permet d'éxecuter une commande
     * @param commande la commande à exécuter
     */
    private int executCommande(String commande)
    {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String shell;
        String shellOption;

        if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
        {
            shell = "cmd.exe";
            shellOption = "/c";
        }
        else
        {
            shell = "bash";
            shellOption = "-c";
        }

        /* Préparation de la commande */
        processBuilder.command(shell, shellOption, commande);

        try
        {
            /* Exécution de la commande */
            Process        process = processBuilder.start();
            BufferedReader reader  = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null)
                if (this.hmArgs.get(this.lstOptions.get(7)[0]) == null)
                    System.out.println(line);

            return process.waitFor();
        }
        catch (Exception e)
        {
            System.out.println(MavenLite.ERROR + "L'exécution de la commande '" + shell + " " + shellOption + " " + commande + "' a échoué.");
            System.exit(1);
        }

        return 1;
    }


    /**
     * Permet de supprimer le fichier contenant la liste des fichiers à compiler
     */
    private void removeCompilList()
    {
        File f = new File(MavenLite.COMPILE_LIST_NAME);
        if (f.exists())
            f.delete();
    }


    /*=======================================*/
    /* Méthodes pour l'exécution des options */
    /*=======================================*/

    // faire en sorte de garder les bonne info et d'adapter le code pour faire en sorte de récupérer les bonnes info dans la méthode parseOptions()

    /**
     * Execute les options passées en paramètre
     * @param map les options à exécuter avec leurs arguments associés
     */
    private void executeOption(List<String[]> lst)
    {
        lst = this.removeUnusedOption(lst);
        for (int i = 0; i < lst.size(); i++)
        {
            String[] opt = lst.get(i);
            if (opt[6].equals("0"))
                continue;

            switch (opt[0])
            {
                case "version":
                    this.version("");
                    break;
                case "libraries":
                    this.hmArgs.put(opt[0], this.libraries(opt[5]));
                    break;
                case "help":
                    this.help(this.lstOptions);
                    break;
                case "export":
                    this.export(opt);
                    break;
                case "maven":
                    System.exit(this.maven());
                    break;
                case "uninstall":
                    this.uninstall();
                    break;
                case "test":
                    this.test();
                    break;
            }

            lst.remove(i--);
        }

        /* Création de l'arborescence */
        if (!this.lstOptions.get(1)[6].equals("0")) {
            String projectName = this.hmArgs.get(this.lstOptions.get(1)[0]) == null ? MavenLite.CREATE : this.hmArgs.get(this.lstOptions.get(1)[0]);
            this.create(projectName);
            System.exit(0);
        }

        /* Compilation */
        if (!this.lstOptions.get(2)[6].equals("0") || !this.lstOptions.get(4)[6].equals("0") || !this.lstOptions.get(5)[6].equals("0"))
            this.compilation();

        /* Lancement */
        if (!this.lstOptions.get(3)[6].equals("0") || !this.lstOptions.get(4)[6].equals("0") || !this.lstOptions.get(5)[6].equals("0"))
            this.launch();
    }



    /*=====================================*/
    /* Méthodes correspondants aux options */
    /*=====================================*/
    /**
     * Affiche la version, le nom de l'auteur et des informations sur l'environnement d'exécution.
     * @param phraseAuteur la phrase à afficher après la version
     */
    private void version(String phraseAuteur)
    {
        String version = "";

        version += MavenLite.BOLD + "Maven Lite " + MavenLite.VERSION + MavenLite.DEFAULT + " " + MavenLite.GREEN_BOLD + phraseAuteur + MavenLite.DEFAULT + "\n";
        version += "Maven Lite home : " + new File(".").getAbsolutePath().substring(0, new File(".").getAbsolutePath().length()-2) + "\n";
        version += "Java version : " + System.getProperty("java.version") + ", vendor : " + System.getProperty("java.vendor") + ", runtime : " + System.getProperty("java.runtime.name") + "\n";
        version += "Default locale : " + System.getProperty("user.language") + "_" + System.getProperty("user.country") + ", platform encoding : " + System.getProperty("file.encoding") + "\n";
        version += "OS name : \"" + System.getProperty("os.name") + "\", version : \"" + System.getProperty("os.version") + "\", architecture : \"" + System.getProperty("os.arch") + "\"";
    
        System.out.println(version);
    }

    /**
     * Permet d'afficher l'aide à partir de la liste des options.
     * @param lst la liste des options
     */
    private void help(List<String[]> lst)
    {
        int optLength = 30;
        int descLength = 50;
        String help = "\nUsage : mvnl [options] [argument]\n\n";
        help += "Options :\n";

        for (String[] s : lst)
        {
            help += String.format("  %-" + (optLength-2) + "s", (String.format("%-5s, ", s[1]) + s[2]));

            /* Écriture de la description */
            String sDesc = s[7];
            int currentLineLength = 0;
            for (String word : sDesc.split(" "))
            {
                if ((currentLineLength + word.length()) > descLength)
                {
                    help += "\n" + String.format("%-" + optLength + "s", "");
                    currentLineLength = 0;
                }

                help += word + " ";
                currentLineLength = currentLineLength + word.length();
            }

            if (s[3].equals(s[4]))
                help += String.format("\n%-" + optLength + "s", "") + "Nombre de paramètre : " + MavenLite.BOLD + s[3] + MavenLite.DEFAULT + ".";
            else
                help += String.format("\n%-" + optLength + "s", "") + "Nombre de paramètre : de " + MavenLite.BOLD + s[3] + MavenLite.DEFAULT + " à " + MavenLite.BOLD + s[4] + MavenLite.DEFAULT + ".";
            
            if (s[5] != null)
                help += String.format("\n%-" + optLength + "s", "") + "Valeur par défaut   : " + MavenLite.BOLD + s[5] + MavenLite.DEFAULT + ".";

            help += "\n\n";
        }

        System.out.println(help);
    }    

    /**
     * Permet de spécifier le dossier contenant les fichiers jar utiliser par le programme.
     * Tout les fichiers jar seront ajoutés au classpath lors de la compilation et du lancement.
     * @param opt Ligne de la liste des options correspondant à l'option -lib (--libraries)
     * @return la liste des fichiers jar depuis la racine du projet à ajouter au classpath sous la forme d'une chaine de caractère séparé par des ':'
     */
    private String libraries(String fileName)
    {
        StringBuilder libraries = new StringBuilder();

        File f = new File(fileName);
        if (f.exists() && f.isDirectory())
        {
            File[] lstFiles = f.listFiles();
            for (File file : lstFiles)
            {
                if (file.isFile())
                {
                    if (file.getName().endsWith(".jar"))
                        libraries.append(file.getPath()).append(":");
                }
                else if (file.isDirectory())
                {
                    libraries.append(this.libraries(file.getPath()));
                }
            }
        }
        else
        {
            if (!f.isDirectory())
                System.out.println(MavenLite.ERROR + "Le fichier '" + fileName + "' n'est pas un dossier.");
            else
                System.out.println(MavenLite.ERROR + "Le dossier '" + fileName + "' n'existe pas.");

            System.exit(1);
        }

        return libraries.toString() == "" ? null : libraries.toString().substring(0, libraries.toString().length()-1);
    }

    /**
     * Permet de charger les options à partir d'un fichier de configuration.
     * Le séparateur sont l'espace et le retour à la ligne.
     * Les options de la ligne de commande prédomine sur les options sur celle du fichier de configuration.
     * @param opt Ligne de liste des options correspondant à l'option -f
     */
    private void file(String fileName)
    {
        try
        {
            StringBuilder sFile = new StringBuilder();
            Scanner sc = new Scanner(new File(fileName));

            while (sc.hasNextLine())
            {
                String line = sc.nextLine().replaceAll("^[\\u0009\\u0020]*", ""); // Supprime les espaces et les tabulations au début de la ligne
                if (!line.startsWith("#") && !line.equals(""))
                    sFile.append(line).append(" ");
            }

            sc.close();

            System.out.println("contenue du fichier de configuration : " + sFile.toString()); // debug
            this.parseOptions(sFile.toString().split(" "));
        }
        catch (Exception e)
        {
            System.out.println(MavenLite.ERROR + "La lecture du fichier '" + fileName + "' a échoué.");
            System.exit(1);
        }
    }

    /**
     * Créer l'arborescence du projet ainsi qu'un fichier de config par défaut.
     * @param opt Ligne de la liste des options correspondant à l'option -cr
     */
    private void create(String projectName)
    {
        /*
        ProjectName
        ├── config
        └── src
            ├── main
            │   └── java
            │       └── App.java
            └── resources
                └── lib
        */

        List<String> lstFoldersProject = new ArrayList<String>();
        List<String> lstFilesProject = new ArrayList<String>();

        /* Création de la liste des dossiers du projet */
        lstFoldersProject.add(projectName);
        lstFoldersProject.add(projectName + File.separator + MavenLite.SOURCE);
        lstFoldersProject.add(projectName + File.separator + MavenLite.OUTPUT);
        lstFoldersProject.add(projectName + File.separator + MavenLite.LIBRARIES);

        if (this.hmArgs.get(this.lstOptions.get(6)[0]) == null)
        {
            /* Création de la liste des fichiers du projet */
            lstFilesProject.add(projectName + File.separator + MavenLite.SOURCE + File.separator + MavenLite.MAIN + ".java");
        }
        else
        {
            /* Création de la liste des dossiers du projet */
            lstFoldersProject.add(lstFoldersProject.get(1) + File.separator + "controller");
            lstFoldersProject.add(lstFoldersProject.get(1) + File.separator + "model");
            lstFoldersProject.add(lstFoldersProject.get(1) + File.separator + "view");

            /* Création de la liste des fichiers du projet */
            lstFilesProject.add(projectName + File.separator + MavenLite.SOURCE + File.separator + "controller" + File.separator + MavenLite.MAIN + ".java");
        }

        /* Création de la liste des fichiers du projet */
        lstFilesProject.add(projectName + File.separator + MavenLite.FILE);


        /* Création de l'arborescence */
        for (String folder : lstFoldersProject)
        {
            File f = new File(folder);
            if (!f.exists())
                f.mkdirs();
        }

        /* Création des fichiers */
        for (String file : lstFilesProject)
        {
            File f = new File(file);
            if (!f.exists())
            {
                try
                {
                    f.createNewFile();
                }
                catch (IOException e)
                {
                    System.out.println(MavenLite.ERROR + "La création du fichier '" + f.getName() + "' a échoué.");
                    System.exit(1);
                }
            }
        }

        /* Écriture dans le fichier App.java */
        try
        {
            StringBuilder sApp = new StringBuilder();
            if (this.hmArgs.get(this.lstOptions.get(6)[0]) != null)
                sApp.append("package controller;\n\n");

            sApp.append("/**\n");
            sApp.append(" * Hello world!\n");
            sApp.append(" * @version 1.0.0\n");
            sApp.append(" * @autor " + System.getProperty("user.name") + "\n");
            sApp.append(" */\n");
            sApp.append("public class App\n");
            sApp.append("{\n");
            sApp.append("    public static void main(String[] args)\n");
            sApp.append("    {\n");
            sApp.append("        System.out.println(\"Hello World!\");\n");
            sApp.append("    }\n");
            sApp.append("}");

            FileWriter fw = new FileWriter(new File(lstFilesProject.get(0)));
            fw.write(sApp.toString());
            fw.close();
        }
        catch (Exception e)
        {
            System.out.println(MavenLite.ERROR + "L'écriture dans le fichier '" + lstFilesProject.get(0) + "' à échoué.");
            System.exit(1);
        }


        /* Écriture dans le fichier de configuration */
        try
        {
            String sConfig = "";
            sConfig += "# Source du projet\n";
            sConfig += "--source " + MavenLite.SOURCE + "\n";
            sConfig += "\n";
            sConfig += "# Dossier de sortie des fichiers compilés\n";
            sConfig += "--output " + MavenLite.OUTPUT + "\n";
            sConfig += "\n";
            sConfig += "# Liste des libraries à ajouter au classpath\n";
            sConfig += "--libraries " + MavenLite.LIBRARIES + "\n";
            sConfig += "\n";
            sConfig += "# Compiler et lancer le projet grâce à la commande 'mvnl -cl'\n";

            FileWriter fw = new FileWriter(new File(lstFilesProject.get(1)));
            fw.write(sConfig);
            fw.close();
        }
        catch (Exception e)
        {
            System.out.println(MavenLite.ERROR + "L'écriture dans le fichier '" + MavenLite.BLUE_BOLD + lstFilesProject.get(1) + MavenLite.DEFAULT + "' à échoué.");
            System.exit(1);
        }

        this.executCommande("cd ./" + projectName);
        System.out.println("\n" + MavenLite.SUCCESS + "Le projet '" + projectName + "' a été créé avec succès.\n");
        System.out.println(MavenLite.INFO    + "Pour " + MavenLite.GREEN_BOLD + "compiler" + MavenLite.DEFAULT + " le projet, exécuter la commande : '" + MavenLite.BLUE_BOLD + "mvnl -c" + MavenLite.DEFAULT + "'.");
        System.out.println(MavenLite.INFO    + "Pour " + MavenLite.GREEN_BOLD + "lancer  " + MavenLite.DEFAULT + " le projet, exécuter la commande : '" + MavenLite.BLUE_BOLD + "mvnl -l" + MavenLite.DEFAULT + "'.");
        System.out.println(MavenLite.INFO    + "Pour " + MavenLite.GREEN_BOLD + "compiler et lancer" + MavenLite.DEFAULT + " le projet à partir des données du fichier de configuration, exécuter la commande : '" + MavenLite.BLUE_BOLD + "mvnl -f -cl" + MavenLite.DEFAULT + "'.\n");
    }

    /**
     * Compile le projet.
     */
    private void compilation()
    {
        /* Variables */
        StringBuilder command = new StringBuilder();

        try
        {
            FileWriter fw = new FileWriter(new File(MavenLite.COMPILE_LIST_NAME));
            fw.write(this.genererCompileList(new File(this.hmArgs.get("source"))));
            fw.close();
        }
        catch (Exception e)
        {
            System.out.println(MavenLite.ERROR + "L'écriture dans le fichier '" + MavenLite.COMPILE_LIST_NAME + "' à échoué.");
            System.exit(1);
        }

        /* Compilation */
        command.append("javac");
        command.append(" -d ").append(this.hmArgs.get("output"));

        if (this.hmArgs.get("libraries") != null)
            command.append(" -cp ").append(this.hmArgs.get("libraries"));
        if (this.hmArgs.get("classpath") != null)
            command.append(":").append(this.hmArgs.get("classpath"));

        command.append(" -encoding ");
        command.append(this.hmArgs.get("encoding"));
        command.append(" @");
        command.append(MavenLite.COMPILE_LIST_NAME);

        if (this.hmArgs.get("verbose") != null)
            System.out.println(MavenLite.INFO + command.toString());

        System.out.println(MavenLite.INFO + "Compilation du projet " + MavenLite.BLUE_BOLD + MavenLite.PROJECT_NAME + MavenLite.DEFAULT + "...");
        if (this.executCommande(command.toString()) != 0)
        {
            System.out.println(MavenLite.ERROR + "La compilation du projet " + MavenLite.BLUE_BOLD + MavenLite.PROJECT_NAME + MavenLite.DEFAULT + " à échoué.");
            this.removeCompilList();
            System.exit(1);
        }
        
        System.out.println(MavenLite.SUCCESS + "Compilation du projet " + MavenLite.BLUE_BOLD + MavenLite.PROJECT_NAME + MavenLite.DEFAULT + " terminé avec succès.");
        this.removeCompilList();
    }

    /**
     * Lance le projet.
     */
    private void launch()
    {
        /* Variables */
        StringBuilder command = new StringBuilder();

        /* Lancement */
        command.append("java -cp ");
        command.append(this.hmArgs.get("output"));

        if (this.hmArgs.get("libraries") != null)
            command.append(":").append(this.hmArgs.get("libraries"));
        if (this.hmArgs.get("classpath") != null)
            command.append(":").append(this.hmArgs.get("classpath"));

        if (this.hmArgs.get("main") == null)
            this.hmArgs.put("main", this.getMainClassName(new File(this.hmArgs.get("source"))));

        command.append(" ").append(this.hmArgs.get("main"));

        if (this.hmArgs.get("argument") != null)
            command.append(" ").append(this.hmArgs.get("argument"));
        if (this.hmArgs.get("arguments") != null)
            command.append(" ").append(this.hmArgs.get("arguments"));

        if (this.hmArgs.get("verbose") != null)
            System.out.println(MavenLite.INFO + command.toString());
        
        System.out.println(MavenLite.INFO + "Lancement du projet : " + MavenLite.BLUE_BOLD + MavenLite.PROJECT_NAME + MavenLite.DEFAULT + "...");
        if (this.executCommande(command.toString()) != 0)
        {
            System.out.println(MavenLite.ERROR + "Le lancement du projet " + MavenLite.BLUE_BOLD + MavenLite.PROJECT_NAME + MavenLite.DEFAULT + " à échoué.");
            System.exit(1);
        }

        System.out.println(MavenLite.SUCCESS + "Lancement du projet " + MavenLite.BLUE_BOLD + MavenLite.PROJECT_NAME + MavenLite.DEFAULT + " terminé avec succès.");
    }

    /**
     * Exporte le projet dans un fichier jar.
     * Le fichier jar sera exporté dans le dossier de sortie.
     * Le nom du fichier jar est le nom du dossier de sortie.
     * @param opt Ligne de la liste des options correspondant à l'option -ex
     */
    private void export(String[] opt)
    {
        System.out.println("Exportation du projet : " + opt[5]);
        // TODO : Exporter le projet dans un fichier .class
    }

    /**
     * Converti le projet en projet maven.
     * @return 0 si la conversion à réussi, 1 sinon
     */
    private int maven()
    {
        System.out.println("Conversion du projet en projet maven");
        // TODO : Convertir le projet en projet maven

        return 0;
    }

    /**
     * Permet de désinstaller les pages de manuel de Maven Lite.
     * @return 0 si la désinstallation à réussi, 1 si une erreur est survenue, 2 si l'utilisateur à annulé la désinstallation
     */
    private int uninstall()
    {
        // Variables
        String[] manPageLangages = {"fr", "en"};

        System.out.print(MavenLite.WARNING + MavenLite.RED_BOLD + "Attention" + MavenLite.DEFAULT + ", vous êtes sur le point de désinstaller Maven Lite. Êtes-vous sûr de vouloir continuer ? (y/n) : ");
        Scanner sc = new Scanner(System.in);
        String reponse = sc.nextLine().toLowerCase();
        sc.close();

        if (!reponse.matches("^[yY]([eE][sS])?$"))
        {
            System.out.println(MavenLite.INFO + "Désinstallation annulée.");
            return 2;
        }

        System.out.println(MavenLite.INFO + "Désinstallation de Maven Lite...");

        // Suppression des pages de manuel
        System.out.println(MavenLite.INFO + "Suppression des pages de manuel dans les différentes langues...");
        for (String langage : manPageLangages)
        {
            String manPageFile = "/usr/local/man/" + langage + "/man1/mvnl.1.gz";
            if (!deleteFile(manPageFile))
            {
                System.out.println(MavenLite.ERROR + "Erreur lors de la suppression du fichier " + MavenLite.BLUE_BOLD + manPageFile + MavenLite.DEFAULT + ".");
                return 1;
            }
            else
                System.out.println(MavenLite.INFO + "Le fichier " + MavenLite.BLUE_BOLD + manPageFile + MavenLite.DEFAULT + " a été supprimé avec succès.");
        }
        System.out.println(MavenLite.SUCCESS + "Pages de manuel supprimées avec succès.");

        return 0;
    }

    /**
     * Supprime un fichier
     * @param filePath le chemin du fichier à supprimer
     * @return true si le fichier a été supprimé, false sinon
     */
    private static boolean deleteFile(String filePath)
    {
        File file = new File(filePath);
        return file.delete();
    }

    /**
     * Permets d'afficher le nom du programme en ascii art, la version, l'auteur et une phrase d'aide.
     */
    private void defaultAffichage()
    {
        String helpStyle = "\n";

        /* Écriture de 'MVNL' en ascii art */
        helpStyle += "        __  ___                          __    _ __      \n";
        helpStyle += "       /  |/  /___ __   _____  ____     / /   (_) /____   \n";
        helpStyle += "      / /|_/ / __ `/ | / / _ \\/ __ \\   / /   / / __/ _ \\  \n";
        helpStyle += "     / /  / / /_/ /| |/ /  __/ / / /  / /___/ / /_/  __/  \n";
        helpStyle += "    /_/  /_/\\__,_/ |___/\\___/_/ /_/  /_____/_/\\__/\\___/   \n\n";



        System.out.println(helpStyle);
        this.version("Par " + MavenLite.AUTHOR);
        System.out.println("\n    " + MavenLite.BOLD + "Utiliser la commande '" + MavenLite.DEFAULT + MavenLite.GREEN_BOLD + "mvnl -h" + MavenLite.DEFAULT + MavenLite.BOLD + "' ou '" + MavenLite.DEFAULT + MavenLite.GREEN_BOLD + "mvnl --help" + MavenLite.DEFAULT + MavenLite.BOLD + "' pour afficher l'aide." + MavenLite.DEFAULT + "\n");
    }



    /*======*/
    /* Main */
    /*======*/
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            new MavenLite().defaultAffichage();
            System.exit(0);
        }
        else if (args.length == 1 && args[0].equals("uninstall_from_mvnl-uninstall"))
        {
            System.exit(new MavenLite().uninstall());
        }

        new MavenLite(args);
    }



    /*======================*/
    /* Méthodes de débogage */
    /*======================*/
    /**
     * Pour le débogage uniquement
     * Affiche les options et leurs arguments associés
     */
    private void printLstTab(List<String[]> lst)
    {
        for (String[] opt : lst)
        {
            for (int i = 0; i < opt.length-1; i++)
                System.out.print(String.format("%-15s", opt[i]) + " | ");

            System.out.println();
        }
    }

    /**
     * Pour le débogage uniquement
     */
    private void test()
    {
        System.out.println("Test");
    }
}

