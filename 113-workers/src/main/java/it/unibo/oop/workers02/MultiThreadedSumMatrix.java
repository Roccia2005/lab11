package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a standard implementation of the calculation.
 *
 */
public class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthread;

    /**
     * Builds a multithreaded list sum.
     *
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length / this.nthread;
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < matrix.length; start += size) {
            workers.add(new Worker(matrix, start, size));
        }
        for (final Worker w: workers) {
            w.start();
        }
        double sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int row;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         *
         * @param matrix
         *            the matrix to sum
         * @param row
         *            the row number 
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        Worker(final double[][] matrix, final int row, final int nelem) {
            super();
            this.matrix = matrix;
            this.row = row;
            this.nelem = nelem;
        }

        @Override
        public synchronized void run() {
            // Println used to show the working ranges for debugging purposes
            System.out.println("Working from row " + row + " to row " + (row + nelem - 1)); // NOPMD
            for (int i = row; i < matrix.length && i < row + nelem; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    this.res += matrix[i][j];
                }
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         *
         * @return the sum of every element in the array
         */
        public synchronized double getResult() {
            return this.res;
        }
    }
}
