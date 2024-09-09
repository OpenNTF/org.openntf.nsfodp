ARG BASEIMAGE=domino-container:V1202_11032022prod
FROM ${BASEIMAGE}
#FROM hclcom/domino:12.0.2

ENV LANG="en_US.UTF-8"
ENV SetupAutoConfigure="1"
ENV SetupAutoConfigureParams="/local/runner/domino-config.json"
ENV DOMINO_DOCKER_STDOUT="yes"

RUN mkdir -p /local/runner && mkdir -p /local/eclipse/eclipse/plugins

COPY --chown=notes:notes domino-config.json /local/runner/
COPY --chown=notes:notes container.link /opt/hcl/domino/notes/latest/linux/osgi/rcp/eclipse/links/container.link
COPY --chown=notes:notes staging/plugins/* /local/eclipse/eclipse/plugins/
COPY --chown=notes:notes JavaOptions.txt /local/JavaOptions.txt