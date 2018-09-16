package Ec2Processor;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Tags({"aws", "learning", "processor"})
@CapabilityDescription("processing Ec2")
public class Ec2Processor extends AbstractProcessor {

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    // AWS Secret
    // AWS Key
    // AWS Region
    public static final PropertyDescriptor SecretKey = new PropertyDescriptor.Builder()
            .name("AWSSecret")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    public static final PropertyDescriptor Key = new PropertyDescriptor.Builder()
            .name("AwsKey")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    public static final PropertyDescriptor Region = new PropertyDescriptor.Builder()
            .name("AWS Region")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("SUCCESS RELATIONSHIP")
            .build();

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
        propertyDescriptors.add(SecretKey);
        propertyDescriptors.add(Key);
        propertyDescriptors.add(Region);
        this.properties = Collections.unmodifiableList(propertyDescriptors);

        Set<Relationship> relationships = new HashSet<>();
        relationships.add(SUCCESS);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession processSession) throws ProcessException {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(processContext.getProperty(Key).getValue(), processContext.getProperty(SecretKey).getValue());
        final AmazonEC2 ec2Clinet = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(processContext.getProperty(Region).getValue())
                .build();
        FlowFile flowFile = processSession.create();

        processSession.write(flowFile, new OutputStreamCallback() {
            @Override
            public void process(OutputStream outputStream) throws IOException {
                boolean done = false;

                DescribeInstancesRequest request = new DescribeInstancesRequest();
                while(!done) {
                    DescribeInstancesResult response = ec2Clinet.describeInstances(request);

                    for(Reservation reservation : response.getReservations()) {
                        for(Instance instance : reservation.getInstances()) {
                            String output = String.format(

                                    "Found instance with id %s, " +
                                            "AMI %s, " +
                                            "type %s, " +
                                            "state %s " +
                                            "and monitoring state %s",
                                    instance.getInstanceId(),
                                    instance.getImageId(),
                                    instance.getInstanceType(),
                                  instance.getState().getName(),
                                    instance.getMonitoring().getState());
                            outputStream.write(output.getBytes());
                        }
                    }

                    request.setNextToken(response.getNextToken());

                    if(response.getNextToken() == null) {
                        done = true;
                    }
                }
            }
        });

        processSession.transfer(flowFile,SUCCESS);



    }

}


