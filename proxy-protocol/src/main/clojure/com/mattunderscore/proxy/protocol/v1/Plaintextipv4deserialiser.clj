
(ns com.mattunderscore.proxy.protocol.v1.Plaintextipv4deserialiser
  (import [com.mattunderscore.tcproxy.io.serialisation AbstractByteBufferDeserialiser NotDeserialisableResult NeedsMoreDataResult])
  (:gen-class
    :extends com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferDeserialiser))

(defn- is-group-separator [byte]
  (= byte (int \.)))

(defn- is-valid-byte [byte]
  (or (<= (int \0) byte (int \9))
      (is-group-separator byte)))

(defn- process-next-byte [seq context]
  (if-let [byte (first seq)]
    (if (is-valid-byte byte)
      (if (or (and (is-group-separator byte)
                   (= 0 (count (:pending context))))
              (and (is-group-separator byte)
                   (= 4 (count (:groups context)))))
        ; Group separator without pending group or too many groups
        (NotDeserialisableResult/create (+ (:processed context) 1))
        (if (= (count (:pending context)) 3)
          ; Group too long
          (NotDeserialisableResult/create (+ (:processed context) 1))
          (if (is-group-separator byte)
            (process-next-byte (rest seq) (-> context
                                              (update-in [:processed] + 1)
                                              (update-in [:groups] conj (:pending context))
                                              (assoc :pending [])))
            (process-next-byte (rest seq) (-> context
                                              (update-in [:processed] + 1)
                                              (update-in [:pending] conj byte))))))
      (NotDeserialisableResult/create (+ (:processed context) 1)))
    (NeedsMoreDataResult/INSTANCE)))

(defn- read-from-sequence [seq]
  (process-next-byte seq {:processed 0 :groups [] :pending []}))

(defn -doRead
  [this buffer]

  (let [byte-seq (repeatedly #(if (.hasRemaining buffer) (.get buffer) nil))]
    (read-from-sequence byte-seq)))
