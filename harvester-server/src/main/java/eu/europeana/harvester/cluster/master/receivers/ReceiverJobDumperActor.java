package eu.europeana.harvester.cluster.master.receivers;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;
import eu.europeana.harvester.cluster.domain.ClusterMasterConfig;
import eu.europeana.harvester.cluster.domain.TaskState;
import eu.europeana.harvester.cluster.domain.messages.DoneProcessing;
import eu.europeana.harvester.cluster.domain.messages.RetrieveUrl;
import eu.europeana.harvester.cluster.domain.messages.inner.GetTask;
import eu.europeana.harvester.cluster.domain.messages.inner.GetTaskStatesPerJob;
import eu.europeana.harvester.cluster.domain.messages.inner.RemoveJob;
import eu.europeana.harvester.db.interfaces.ProcessingJobDao;
import eu.europeana.harvester.domain.JobState;
import eu.europeana.harvester.domain.ProcessingJob;
import eu.europeana.harvester.logging.LoggingComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReceiverJobDumperActor extends UntypedActor {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Contains all the configuration needed by this actor.
     */
    private final ClusterMasterConfig clusterMasterConfig;

    /**
     * A wrapper class for all important data (ips, loaded jobs, jobs in progress etc.)
     */
    private ActorRef accountantActor;




    /**
     * ProcessingJob DAO object which lets us to read and store data to and from the database.
     */
    private final ProcessingJobDao processingJobDao;





    public ReceiverJobDumperActor(final ClusterMasterConfig clusterMasterConfig,
                                  final ActorRef accountantActor,
                                  final ProcessingJobDao processingJobDao){
        LOG.info(LoggingComponent.appendAppFields(LoggingComponent.Master.TASKS_RECEIVER),
                "ReceiverJobDumperActor constructor");

        this.clusterMasterConfig = clusterMasterConfig;
        this.accountantActor = accountantActor;
        this.processingJobDao = processingJobDao;
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if(message instanceof DoneProcessing) {
            final DoneProcessing doneProcessing = (DoneProcessing) message;
            markDone(doneProcessing);
            return;
        }
    }



    /**
     * Marks task as done and save it's statistics in the DB.
     * If one job has finished all his tasks then the job also will be marked as done(FINISHED).
     * @param msg - the message from the slave actor with url, jobId and other statistics
     */
    private void markDone(DoneProcessing msg) {

        final String jobId = msg.getJobId();

        List<TaskState> taskStates = new ArrayList<>();
        final Timeout timeout = new Timeout(Duration.create(10, TimeUnit.SECONDS));
        Future<Object> future = Patterns.ask(accountantActor, new GetTaskStatesPerJob(jobId), timeout);
        try {
            taskStates = (List<TaskState>) Await.result(future, timeout.duration());
        } catch (Exception e) {
            LOG.error(LoggingComponent.appendAppFields(LoggingComponent.Master.TASKS_RECEIVER),
                    "Error at markDone->GetTaskStatesPerJob.", e);
            // TODO : Investigate if it make sense to hide the exception here.
        }

        RetrieveUrl retrieveUrl = null;
        future = Patterns.ask(accountantActor, new GetTask(msg.getTaskID()), timeout);
        try {
            retrieveUrl = (RetrieveUrl) Await.result(future, timeout.duration());
        } catch (Exception e) {
            LOG.error(LoggingComponent.appendAppFields(LoggingComponent.Master.TASKS_RECEIVER),
                    "Error at markDone->GetTask", e);
            // TODO : Investigate if it make sense to hide the exception here.

        }

        if(retrieveUrl != null && !retrieveUrl.getId().equals("")) {
            final String ipAddress = retrieveUrl.getIpAddress();
            checkJobStatus(jobId, taskStates, ipAddress);
        }
    }


    /**
     * Checks if a job is done, and if it's done than generates an event.
     * @param jobID the unique ID of the job
     * @param states all request states the job
     */
    private void checkJobStatus(final String jobID, final List<TaskState> states, final String ipAddress) {
        boolean allDone = true;
        for (final TaskState state : states) {
            if(!(TaskState.DONE).equals(state)) {
                allDone = false;
                break;
            }
        }

        if(allDone) {
            //LOG.info("Job dumper checkjobstatus - Marking job {} as processed and removing it from accounting", jobID);
            accountantActor.tell(new RemoveJob(jobID,ipAddress), getSelf());
            final ProcessingJob processingJob = processingJobDao.read(jobID);
            final ProcessingJob newProcessingJob = processingJob.withState(JobState.FINISHED);
            processingJobDao.update(newProcessingJob, clusterMasterConfig.getWriteConcern());



        }
    }
}
