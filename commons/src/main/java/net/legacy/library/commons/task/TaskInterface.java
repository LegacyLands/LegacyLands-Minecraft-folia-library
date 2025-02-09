package net.legacy.library.commons.task;

import io.fairyproject.mc.scheduler.MCScheduler;
import io.fairyproject.mc.scheduler.MCSchedulers;
import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.repeat.RepeatPredicate;
import io.fairyproject.scheduler.response.TaskResponse;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * The {@code TaskInterface} provides a high-level abstraction for scheduling and running tasks
 * using the {@link MCScheduler} (usually provided by {@link MCSchedulers}).
 *
 * <p>This interface simplifies task scheduling by providing convenient methods with consistent
 * naming and parameter orders.
 *
 * <p>By default, the scheduler returned from {@link #getMCScheduler()} is the asynchronous scheduler
 * ({@link MCSchedulers#getAsyncScheduler()}); however, implementations can override this method
 * to return other schedulers if needed.
 *
 * <p>This interface is intended for classes that encapsulate their own internal scheduling logic.
 * For instance, a class implementing this interface might define how and when tasks start by
 * overriding the {@link #start()} method.
 *
 * @author qwq-dev
 * @see MCSchedulers
 * @see MCScheduler
 * @see io.fairyproject.scheduler.Scheduler
 * @since 2024-12-14 12:30
 */
public interface TaskInterface {
    /**
     * Starts the task. Implementations should define the logic of the task within this method.
     *
     * <p>By default, overriding this method will schedule the task asynchronously, depending on the
     * {@link MCScheduler} provided by {@link #getMCScheduler()}. This method could, for example,
     * schedule periodic tasks or a single one-time task.
     *
     * @return a {@link ScheduledTask} representing the started task
     */
    ScheduledTask<?> start();

    /**
     * Provides the {@link MCScheduler} instance used for scheduling tasks.
     *
     * <p>By default, this returns {@code MCSchedulers.getAsyncScheduler()}, but implementations can
     * override it to return a different scheduler (e.g., synchronous, or a custom one).
     *
     * @return the {@link MCScheduler} scheduler instance
     */
    default MCScheduler getMCScheduler() {
        return MCSchedulers.getAsyncScheduler();
    }

    /**
     * Schedule a one-time {@link Runnable} task with a specified delay in ticks.
     *
     * @param task       the task to be executed
     * @param delayTicks the delay in ticks before the task is executed
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default ScheduledTask<?> schedule(Runnable task, long delayTicks) {
        return getMCScheduler().schedule(task, delayTicks);
    }

    /**
     * Schedule a periodic {@link Runnable} task with a fixed delay and interval in ticks.
     *
     * @param task          the task to be executed
     * @param delayTicks    the initial delay in ticks before the first execution
     * @param intervalTicks the interval in ticks between consecutive executions
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default ScheduledTask<?> scheduleAtFixedRate(Runnable task, long delayTicks, long intervalTicks) {
        return getMCScheduler().scheduleAtFixedRate(task, delayTicks, intervalTicks);
    }

    /**
     * Schedule a periodic {@link Runnable} task with a fixed delay and interval in ticks, and a custom
     * {@link RepeatPredicate}.
     *
     * <p>The {@link RepeatPredicate} can be used to dynamically determine whether the task should continue
     * running or be terminated based on the logic you define.
     *
     * @param task          the task to be executed
     * @param delayTicks    the initial delay in ticks before the first execution
     * @param intervalTicks the interval in ticks between consecutive executions
     * @param predicate     the {@link RepeatPredicate} to control repetition
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default ScheduledTask<?> scheduleAtFixedRate(Runnable task, long delayTicks, long intervalTicks, RepeatPredicate<?> predicate) {
        return getMCScheduler().scheduleAtFixedRate(task, delayTicks, intervalTicks, predicate);
    }

    /**
     * Schedule a one-time {@link Callable} task with a specified delay in ticks.
     *
     * <p>The returned {@link ScheduledTask} will also carry the result of the callable upon completion.
     *
     * @param task       the callable task to be executed
     * @param delayTicks the delay in ticks before the task is executed
     * @param <R>        the return type of the callable
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default <R> ScheduledTask<R> schedule(Callable<R> task, long delayTicks) {
        return getMCScheduler().schedule(task, delayTicks);
    }

    /**
     * Schedule a periodic {@link Callable} task with a fixed delay and interval in ticks.
     *
     * <p>The callable is expected to return a {@link TaskResponse} which defines whether the task
     * should continue or terminate.
     *
     * @param task          the callable task to be executed
     * @param delayTicks    the initial delay in ticks before the first execution
     * @param intervalTicks the interval in ticks between consecutive executions
     * @param <R>           the return type wrapped by {@link TaskResponse}
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> task, long delayTicks, long intervalTicks) {
        return getMCScheduler().scheduleAtFixedRate(task, delayTicks, intervalTicks);
    }

    /**
     * Schedule a periodic {@link Callable} task with a fixed delay and interval in ticks, and a custom
     * {@link RepeatPredicate}.
     *
     * <p>The {@link RepeatPredicate} will control the repetition logic using the callable's return value.
     *
     * @param task          the callable task to be executed
     * @param delayTicks    the initial delay in ticks before the first execution
     * @param intervalTicks the interval in ticks between consecutive executions
     * @param predicate     the {@link RepeatPredicate} to control repetition
     * @param <R>           the return type wrapped by {@link TaskResponse}
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> task, long delayTicks, long intervalTicks, RepeatPredicate<R> predicate) {
        return getMCScheduler().scheduleAtFixedRate(task, delayTicks, intervalTicks, predicate);
    }

    /**
     * Schedule a one-time {@link Runnable} task without any initial delay (i.e., run as soon as possible).
     *
     * @param task the task to be executed
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default ScheduledTask<?> schedule(Runnable task) {
        return getMCScheduler().schedule(task);
    }

    /**
     * Schedule a one-time {@link Runnable} task with a specified {@link Duration} delay.
     *
     * @param task  the task to be executed
     * @param delay the duration before the task is executed
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default ScheduledTask<?> schedule(Runnable task, Duration delay) {
        return getMCScheduler().schedule(task, delay);
    }

    /**
     * Schedule a periodic {@link Runnable} task using {@link Duration} based initial delay and interval.
     *
     * @param task     the task to be executed
     * @param delay    the initial delay before the first execution
     * @param interval the interval between consecutive executions
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default ScheduledTask<?> scheduleAtFixedRate(Runnable task, Duration delay, Duration interval) {
        return getMCScheduler().scheduleAtFixedRate(task, delay, interval);
    }

    /**
     * Schedule a periodic {@link Runnable} task using {@link Duration} based initial delay and interval,
     * with a custom {@link RepeatPredicate}.
     *
     * @param task      the task to be executed
     * @param delay     the initial delay before the first execution
     * @param interval  the interval between consecutive executions
     * @param predicate the {@link RepeatPredicate} to control repetition
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default ScheduledTask<?> scheduleAtFixedRate(Runnable task, Duration delay, Duration interval, RepeatPredicate<?> predicate) {
        return getMCScheduler().scheduleAtFixedRate(task, delay, interval, predicate);
    }

    /**
     * Schedule a one-time {@link Callable} task with no initial delay.
     *
     * @param task the callable task to be executed
     * @param <R>  the return type of the callable
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default <R> ScheduledTask<R> schedule(Callable<R> task) {
        return getMCScheduler().schedule(task);
    }

    /**
     * Schedule a one-time {@link Callable} task with a specified {@link Duration} delay.
     *
     * @param task  the callable task to be executed
     * @param delay the duration before the task is executed
     * @param <R>   the return type of the callable
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default <R> ScheduledTask<R> schedule(Callable<R> task, Duration delay) {
        return getMCScheduler().schedule(task, delay);
    }

    /**
     * Schedule a periodic {@link Callable} task using {@link Duration} based initial delay and interval.
     *
     * <p>The callable is expected to return a {@link TaskResponse} that informs whether the task should continue.
     *
     * @param task     the callable task to be executed
     * @param delay    the initial delay before the first execution
     * @param interval the interval between consecutive executions
     * @param <R>      the return type wrapped by {@link TaskResponse}
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> task, Duration delay, Duration interval) {
        return getMCScheduler().scheduleAtFixedRate(task, delay, interval);
    }

    /**
     * Schedule a periodic {@link Callable} task using {@link Duration} based initial delay and interval,
     * with a custom {@link RepeatPredicate}.
     *
     * @param task      the callable task to be executed
     * @param delay     the initial delay before the first execution
     * @param interval  the interval between consecutive executions
     * @param predicate the {@link RepeatPredicate} to control repetition
     * @param <R>       the return type wrapped by {@link TaskResponse}
     * @return a {@link ScheduledTask} representing the scheduled task
     */
    default <R> ScheduledTask<R> scheduleAtFixedRate(Callable<TaskResponse<R>> task, Duration delay, Duration interval, RepeatPredicate<R> predicate) {
        return getMCScheduler().scheduleAtFixedRate(task, delay, interval, predicate);
    }

    /**
     * Check if the current thread is the same thread used by the scheduler returned
     * by {@link #getMCScheduler()}.
     *
     * @return {@code true} if the current thread is the scheduler's thread, {@code false} otherwise
     */
    default boolean isCurrentThread() {
        return getMCScheduler().isCurrentThread();
    }

    /**
     * Execute a {@link Runnable} task immediately, typically using the {@link MCScheduler#execute(Runnable)}
     * method of the configured scheduler. This is useful for tasks you want to run as soon as possible,
     * without any delay or periodic repetition.
     *
     * @param task the task to be executed
     */
    default void execute(Runnable task) {
        getMCScheduler().execute(task);
    }
}
