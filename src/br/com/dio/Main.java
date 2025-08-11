package br.com.dio;

import br.com.dio.model.Board;
import br.com.dio.model.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static br.com.dio.util.BoardTemplate.BOARD_TEMPLATE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class Main {

    private final static Scanner scanner = new Scanner(System.in);

    private static Board board;

    private final static int BOARD_LIMIT = 9;

    public static void main(String[] args) {
        final var positions = Stream.of(args)
                .collect(toMap(
                        k -> k.split(";")[0],
                        v -> v.split(";")[1]));
        var option = -1;
        while (true) {
            System.out.println("Selecione uma das opções a seguir");
            System.out.println("1 - Iniciar um novo Jogo");
            System.out.println("2 - Colocar um novo número");
            System.out.println("3 - Remover um número");
            System.out.println("4 - Visualizar jogo atual");
            System.out.println("5 - Verificar status do jogo");
            System.out.println("6 - limpar jogo");
            System.out.println("7 - Finalizar jogo");
            System.out.println("8 - Sair");

            option = scanner.nextInt();

            switch (option) {
                case 1 -> startGame(positions);
                case 2 -> inputNumber();
                case 3 -> removeNumber();
                case 4 -> showCurrentGame();
                case 5 -> showGameStatus();
                case 6 -> clearGame();
                case 7 -> finishGame();
                case 8 -> System.exit(0);
                default -> System.out.println("Opção inválida, selecione uma das opções do menu");
            }
        }
    }

    private static void startGame(final Map<String, String> positions) {
        if (nonNull(board)) {
            System.out.println("O jogo já foi iniciado");
            return;
        }

        // Geração aleatória de Sudoku
        int[][] boardFull = new int[BOARD_LIMIT][BOARD_LIMIT];
        fillSudoku(boardFull, 0, 0);

        // Remove aleatoriamente valores para criar dicas iniciais
        int clues = 30 + (int) (Math.random() * 11); // entre 30 e 40 dicas
        boolean[][] fixedValues = new boolean[BOARD_LIMIT][BOARD_LIMIT];
        int[][] initialValues = new int[BOARD_LIMIT][BOARD_LIMIT];
        // Copia o tabuleiro completo
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (int j = 0; j < BOARD_LIMIT; j++) {
                initialValues[i][j] = boardFull[i][j];
                fixedValues[i][j] = true;
            }
        }
        // Remove valores até restar apenas o número de dicas
        int removed = 0;
        while (81 - removed > clues) {
            int i = (int) (Math.random() * BOARD_LIMIT);
            int j = (int) (Math.random() * BOARD_LIMIT);
            if (initialValues[i][j] != 0) {
                initialValues[i][j] = 0;
                fixedValues[i][j] = false;
                removed++;
            }
        }

        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                int expected = initialValues[i][j];
                boolean fixed = fixedValues[i][j];
                // Se houver configuração customizada, sobrescreve
                var positionConfig = positions.get("%s,%s".formatted(i, j));
                if (positionConfig != null) {
                    var split = positionConfig.split(",");
                    expected = Integer.parseInt(split[0]);
                    fixed = Boolean.parseBoolean(split[1]);
                }
                var currentSpace = new Space(expected, fixed);
                spaces.get(i).add(currentSpace);
            }
        }

        board = new Board(spaces);
        System.out.println("O jogo está pronto para começar");
        // Função auxiliar para preencher o Sudoku
    }

    // Algoritmo simples de backtracking para preencher o Sudoku
    private static boolean fillSudoku(int[][] board, int row, int col) {
        if (row == BOARD_LIMIT)
            return true;
        int nextRow = (col == BOARD_LIMIT - 1) ? row + 1 : row;
        int nextCol = (col + 1) % BOARD_LIMIT;
        List<Integer> nums = new ArrayList<>();
        for (int n = 1; n <= BOARD_LIMIT; n++)
            nums.add(n);
        java.util.Collections.shuffle(nums);
        for (int num : nums) {
            if (isSafe(board, row, col, num)) {
                board[row][col] = num;
                if (fillSudoku(board, nextRow, nextCol))
                    return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private static boolean isSafe(int[][] board, int row, int col, int num) {
        for (int i = 0; i < BOARD_LIMIT; i++) {
            if (board[row][i] == num || board[i][col] == num)
                return false;
        }
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[boxRow + i][boxCol + j] == num)
                    return false;
            }
        }
        return true;
    }

    private static void inputNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado iniciado");
            return;
        }

        System.out.println("Informe a coluna que em que o número será inserido");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha que em que o número será inserido");
        var row = runUntilGetValidNumber(0, 8);
        System.out.printf("Informe o número que vai entrar na posição [%s,%s]\n", col, row);
        var value = runUntilGetValidNumber(1, 9);
        if (!board.changeValue(col, row, value)) {
            System.out.printf("A posição [%s,%s] tem um valor fixo\n", col, row);
        }
    }

    private static void removeNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado iniciado");
            return;
        }

        System.out.println("Informe a coluna que em que o número será inserido");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha que em que o número será inserido");
        var row = runUntilGetValidNumber(0, 8);
        if (!board.clearValue(col, row)) {
            System.out.printf("A posição [%s,%s] tem um valor fixo\n", col, row);
        }
    }

    private static void showCurrentGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado iniciado");
            return;
        }

        var args = new Object[81];
        var argPos = 0;
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (var col : board.getSpaces()) {
                args[argPos++] = " " + ((isNull(col.get(i).getActual())) ? " " : col.get(i).getActual());
            }
        }
        System.out.println("Seu jogo se encontra da seguinte forma");
        System.out.printf((BOARD_TEMPLATE) + "\n", args);
    }

    private static void showGameStatus() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado iniciado");
            return;
        }

        System.out.printf("O jogo atualmente se encontra no status %s\n", board.getStatus().getLabel());
        if (board.hasErrors()) {
            System.out.println("O jogo contém erros");
        } else {
            System.out.println("O jogo não contém erros");
        }
    }

    private static void clearGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado iniciado");
            return;
        }

        System.out.println("Tem certeza que deseja limpar seu jogo e perder todo seu progresso?");
        var confirm = scanner.next();
        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")) {
            System.out.println("Informe 'sim' ou 'não'");
            confirm = scanner.next();
        }

        if (confirm.equalsIgnoreCase("sim")) {
            board.reset();
        }
    }

    private static void finishGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado iniciado");
            return;
        }

        if (board.gameIsFinished()) {
            System.out.println("Parabéns você concluiu o jogo");
            showCurrentGame();
            board = null;
        } else if (board.hasErrors()) {
            System.out.println("Seu jogo conté, erros, verifique seu board e ajuste-o");
        } else {
            System.out.println("Você ainda precisa preenhcer algum espaço");
        }
    }

    private static int runUntilGetValidNumber(final int min, final int max) {
        var current = scanner.nextInt();
        while (current < min || current > max) {
            System.out.printf("Informe um número entre %s e %s\n", min, max);
            current = scanner.nextInt();
        }
        return current;
    }

}
