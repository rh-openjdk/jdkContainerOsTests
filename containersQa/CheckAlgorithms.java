import java.io.File;
import java.io.FileNotFoundException;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CheckAlgorithms {
    private static final List<String> possibleFirstArgs = Arrays.asList("assert", "true", "silent-assert", "list", "false");
    private static final List<String> possibleSecondArgs = Arrays.asList("algorithms", "providers", "both");

    // general purpose variables used later
    public static List<String> FIPS_PROVIDERS = new ArrayList<>();
    public static List<String> NONFIPS_PROVIDERS = new ArrayList<>();
    public static List<String> FIPS_ALGORITHMS = new ArrayList<>();
    public static List<String> NONFIPS_ALGORITHMS = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3 || args[0].equals("--help") || args[0].equals("-h")) {
            System.err.println("Test for listing available algorithms and providers and checking their FIPS compatibility");
            System.err.println("Usage: CheckAlgorithms " + possibleFirstArgs + " " + possibleSecondArgs + " configFilePath");
            System.err.println("First argument: specify whether to check FIPS compatibility (assert/true) or just list the items (list/false)");
            System.err.println("                silent-asserts just asserts, not lists the items");
            System.err.println("Second argument: specify what to check - algorithms, providers or both");
            System.err.println("Third argument: mandatory when asserting, specify a config file with algorithms and providers the test should check against");
            System.err.println("                the file can contain 4 different headings and then algorithms/providers belonging to the heading - one on each line");
            System.err.println("                the different headings are: [providers-contains], [providers-not-contains], [algorithms-contains] and [algorithms-not-contains]");
            System.exit(1);
        }

        // Parse the arguments
        String shouldHonorFips = args[0].toLowerCase();
        String testCategory = args[1].toLowerCase();
        String configFilePath = "";
        if (args.length == 3) {
            configFilePath = args[2];
        }

        // Check if the shouldHonorFips is valid value
        if (!possibleFirstArgs.contains(shouldHonorFips)) {
            System.err.println("Invalid value for the first argument: '" + args[0] + "', use --help for more info.");
            System.exit(1);
        }

        // Check if the testCategory is valid value
        if (!possibleSecondArgs.contains(testCategory)) {
            System.err.println("Invalid test category: '" + args[1] + "', use --help for more info.");
            System.exit(1);
        }

        System.out.println("Test type: " + shouldHonorFips);
        System.out.println("What is tested: " + testCategory);
        System.out.println("Config file: " + configFilePath);

        boolean listItems = !shouldHonorFips.equals("silent-assert");
        boolean honorFipsHere = shouldHonorFips.equals("assert") || shouldHonorFips.equals("true") || shouldHonorFips.equals("silent-assert");

        if (configFilePath.isEmpty() && honorFipsHere) {
            // User must specify a config file when asserting
            System.err.println("Must specify a config file, when asserting!");
            System.exit(1);
        } else if (honorFipsHere) {
            // parse the config file
            File configFile = new File(configFilePath);
            parseConfigFile(configFile);
        }

        boolean algorithmsOk = true;
        boolean providersOk = true;
        if (testCategory.equals("algorithms") || testCategory.equals("both")){
            algorithmsOk = checkAlgorithms(listItems, honorFipsHere);
        }
        if (testCategory.equals("providers") || testCategory.equals("both")) {
            providersOk = checkProviders(listItems, honorFipsHere);
        }

        // throwing the correct exception based on what failed (even if both failed)
        if (!algorithmsOk && !providersOk) {
            throw new Exception("Both algorithms and providers contain wrong or don't contain correct items.");
        } else if (!algorithmsOk) {
            throw new Exception("Algorithms contain wrong or don't contain correct items.");
        } else if (!providersOk) {
            throw new Exception("Providers contain wrong or don't contain correct items.");
        }
    }

    static boolean checkProviders(boolean listItems, boolean shouldHonorFips) {
        System.out.println(">>>CHECKING PROVIDERS<<<");

        // print all providers
        if (listItems) {
            System.out.println("LIST OF PROVIDERS:");
            for(Provider provider : Security.getProviders()){
                System.out.println("  " + provider);
            }
        }

        if (shouldHonorFips) {
            System.out.println("ASSERTING FIPS PROVIDERS:");
            return providerAssert();
        }

        return true; // no assertion = everything is ok
    }

    static boolean checkAlgorithms(boolean listItems, boolean shouldHonorFips) throws Exception{
        System.out.println(">>>CHECKING ALGORITHMS<<<");

        // print all algorithms
        if (listItems) {
            System.out.println("LIST OF ALGORITHMS:");
            for (String cipher : CipherList.getCipherList()) {
                System.out.println("  " + cipher);
            }
        }

        if (shouldHonorFips) {
            System.out.println("ASSERTING FIPS ALGORITHMS:");
            return algorithmAssert();
        }

        return true; // no assertion = everything is ok
    }

    static boolean algorithmAssert() throws Exception {
        List<String> algorithms = Arrays.asList(CipherList.getCipherList());
        boolean allOk = true;

        // assert that algorithms list contains all FIPS_ALGORITHMS
        for (String fips : FIPS_ALGORITHMS) {
            System.out.print("  asserting algorithms contain '" + fips + "' - ");
            if (algorithms.contains(fips)) {
                System.out.println("OK");
            } else {
                System.out.println("FAIL");
                allOk = false;
            }
        }

        // assert that algorithms list doesn't contain any NONFIPS_ALGORITHMS
        for (String nonFips : NONFIPS_ALGORITHMS) {
            System.out.print("  asserting algorithms don't contain '" + nonFips + "' - ");
            if (!algorithms.contains(nonFips)) {
                System.out.println("OK");
            } else {
                System.out.println("FAIL");
                allOk = false;
            }
        }

        return allOk;
    }

    static boolean providerAssert() {
        Provider[] providers = Security.getProviders();
        boolean ok;
        boolean ret = true;

        // assert that providers list contains all FIPS_PROVIDERS
        for (String fips : FIPS_PROVIDERS) {
            System.out.print("  asserting providers contain '" + fips + "' - ");

            ok = false;
            for (Provider p : providers) {
                if (p.contains(fips)) {
                    System.out.println("OK");
                    ok = true;
                    break;
                }
            }

            if (!ok) {
                System.out.println("FAIL");
                ret = false;
            }
        }

        // assert that providers list doesn't contain any NONFIPS_PROVIDERS
        for (String nonFips : NONFIPS_PROVIDERS) {
            System.out.print("  asserting providers don't contain '" + nonFips + "' - ");

            ok = true;
            for (Provider p : providers) {
                if (p.contains(nonFips)) {
                    System.out.println("FAIL");
                    ok = false;
                    ret = false;
                    break;
                }
            }

            if (ok) {
                System.out.println("OK");
            }
        }

        return ret;
    }

    static void parseConfigFile(File configFile) {
        try {
            Scanner fileReader = new Scanner(configFile);

            String listType = "";
            List<String> currentList = new ArrayList<>();

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();

                if (line.startsWith("[") && line.endsWith("]")) {
                    if (!currentList.isEmpty()) {
                        flushList(listType, currentList);
                    }
                    listType = line;
                    currentList.clear();
                } else if (!line.isEmpty()) {
                    currentList.add(line);
                }
            }

            if (!currentList.isEmpty()) {
                flushList(listType, currentList);
            }

        } catch (FileNotFoundException e) {
            System.err.println("Specified config file was not found!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    static void flushList(String listType, List<String> listFromConfig) {
        switch (listType) {
            case "[providers-contains]":
                FIPS_PROVIDERS.addAll(listFromConfig);
                break;
            case "[providers-not-contains]":
                NONFIPS_PROVIDERS.addAll(listFromConfig);
                break;
            case "[algorithms-contains]":
                FIPS_ALGORITHMS.addAll(listFromConfig);
                break;
            case "[algorithms-not-contains]":
                NONFIPS_ALGORITHMS.addAll(listFromConfig);
                break;
            default:
                System.err.println("Unknown " + listType + " category from config file!");
                System.exit(1);
        }
    }
}
